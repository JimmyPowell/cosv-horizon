package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.service.*;
import tech.cspioneer.backend.enums.UserStatus;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证与注册")
@Slf4j
public class AuthController {
    private final VerificationCodeService codeService;
    private final RegistrationSessionService regSessionService;
    private final EmailService emailService;
    private final RateLimiterService rateLimiterService;
    private final UserService userService;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final PasswordResetCodeService pwdCodeService;
    private final PasswordResetSessionService pwdSessionService;
    private final TokenRevocationService tokenRevocationService;

    public AuthController(VerificationCodeService codeService,
                          RegistrationSessionService regSessionService,
                          EmailService emailService,
                          RateLimiterService rateLimiterService,
                          UserService userService,
                          JwtService jwtService,
                          TokenBlacklistService blacklistService,
                          PasswordResetCodeService pwdCodeService,
                          PasswordResetSessionService pwdSessionService,
                          TokenRevocationService tokenRevocationService) {
        this.codeService = codeService;
        this.regSessionService = regSessionService;
        this.emailService = emailService;
        this.rateLimiterService = rateLimiterService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.blacklistService = blacklistService;
        this.pwdCodeService = pwdCodeService;
        this.pwdSessionService = pwdSessionService;
        this.tokenRevocationService = tokenRevocationService;
    }

    public static class RequestCodeReq {
        @NotBlank @Email public String email;
    }

    @PostMapping("/register/request-code")
    @Operation(summary = "注册-请求验证码")
    public ApiResponse<Map<String, Object>> requestCode(@Valid @RequestBody RequestCodeReq req, HttpServletRequest httpReq) {
        String ip = clientIp(httpReq);
        if (!rateLimiterService.allowIp(ip) || !rateLimiterService.allowEmail(req.email)) {
            throw new ApiException(1007, "请求过于频繁，请稍后再试");
        }
        VerificationCodeService.CodeIssueResult issued = codeService.issueCode(req.email);
        emailService.sendVerificationCode(req.email, issued.code, codeService.ttlMinutes());
        Map<String, Object> data = new HashMap<>();
        data.put("requestId", issued.requestId);
        return ApiResponse.success(data);
    }

    public static class VerifyCodeReq {
        @NotBlank @Email public String email;
        @NotBlank public String code;
        @NotBlank public String requestId;
    }

    @PostMapping("/register/verify-code")
    @Operation(summary = "注册-验证验证码，发放注册会话")
    public ApiResponse<Map<String, Object>> verifyCode(@Valid @RequestBody VerifyCodeReq req) {
        boolean ok = codeService.verifyAndConsume(req.email, req.code, req.requestId);
        if (!ok) throw new ApiException(1002, "验证码无效或已过期");
        String regSession = regSessionService.createSession(req.email);
        Map<String, Object> data = new HashMap<>();
        data.put("regSession", regSession);
        return ApiResponse.success(data);
    }

    public static class CompleteReq {
        @NotBlank public String regSession;
        @NotBlank public String username;
        @NotBlank public String password;
        public String realName;
        public String company;
        public String location;
    }

    @PostMapping("/register/complete")
    @Operation(summary = "注册-完成用户创建（不返回token）")
    public ApiResponse<Map<String, Object>> complete(@Valid @RequestBody CompleteReq req) {
        String email = regSessionService.consumeSession(req.regSession);
        if (email == null) throw new ApiException(1003, "注册会话无效或已过期");
        User user = userService.registerUser(email, req.username, req.password, req.realName, req.company, req.location);
        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(user));
        return ApiResponse.success(data);
    }

    public static class LoginReq {
        @NotBlank public String login; // email or username
        @NotBlank public String password;
    }

    @PostMapping("/login")
    @Operation(summary = "登录，返回用户信息与双token")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        User user = userService.findByLogin(req.login);
        if (user == null || !userService.verifyPassword(user, req.password)) {
            throw new ApiException(1004, "账号或密码错误");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(1005, "用户状态异常");
        }
        String access = jwtService.generateAccessToken(user.getUuid(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getUuid(), user.getRole().name(), null);

        Map<String, Object> data = new HashMap<>();
        data.put("user", userView(withoutPassword(user)));
        data.put("accessToken", access);
        data.put("accessTokenExpiresIn", jwtService.getAccessTtl().toSeconds());
        data.put("refreshToken", refresh);
        data.put("refreshTokenExpiresIn", jwtService.getRefreshTtl().toSeconds());
        log.info("user login success userUuid={} login={} role={}", user.getUuid(), req.login, user.getRole());
        return ApiResponse.success(data);
    }

    public static class LogoutReq {
        @NotBlank public String refreshToken;
    }

    @PostMapping("/logout")
    @Operation(summary = "登出，吊销本次refresh token")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutReq req) {
        try {
            JwtService.TokenInfo info = jwtService.parseRefreshToken(req.refreshToken);
            long ttl = jwtService.ttlSecondsUntil(info.getExpiresAt());
            blacklistService.blacklistRefreshJti(info.getJti(), ttl);
            log.info("user logout success userUuid={}", info.getSubjectUuid());
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(1008, "Token 无效或已过期");
        }
    }

    public static class RefreshReq {
        @NotBlank public String refreshToken;
    }

    @PostMapping("/refresh")
    @Operation(summary = "使用refresh获取新的access与refresh（轮换旧refresh）")
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody RefreshReq req) {
        try {
            JwtService.TokenInfo oldInfo = jwtService.parseRefreshToken(req.refreshToken);
            // 1) 被黑名单吊销
            if (blacklistService.isRefreshBlacklisted(oldInfo.getJti())) {
                return ApiResponse.error(1008, "Token 无效或已过期");
            }
            // 2) 按用户invalidAfter全量吊销（密码重置等）
            if (!tokenRevocationService.isIssuedAfter(oldInfo.getSubjectUuid(), oldInfo.getIssuedAt())) {
                return ApiResponse.error(1008, "Token 无效或已过期");
            }
            long ttl = jwtService.ttlSecondsUntil(oldInfo.getExpiresAt());
            blacklistService.blacklistRefreshJti(oldInfo.getJti(), ttl);

            String newAccess = jwtService.generateAccessToken(oldInfo.getSubjectUuid(), oldInfo.getRole());
            String newRefresh = jwtService.generateRefreshToken(oldInfo.getSubjectUuid(), oldInfo.getRole(), null);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccess);
            data.put("accessTokenExpiresIn", jwtService.getAccessTtl().toSeconds());
            data.put("refreshToken", newRefresh);
            data.put("refreshTokenExpiresIn", jwtService.getRefreshTtl().toSeconds());
            log.info("token refresh success userUuid={} role={}", oldInfo.getSubjectUuid(), oldInfo.getRole());
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(1008, "Token 无效或已过期");
        }
    }

    // ======================== Password Reset Flow ========================
    public static class PwdRequestCodeReq {
        @NotBlank @Email public String email;
    }

    @PostMapping("/password/request-code")
    @Operation(summary = "重置密码-请求验证码")
    public ApiResponse<Map<String, Object>> passwordRequestCode(@Valid @RequestBody PwdRequestCodeReq req, HttpServletRequest httpReq) {
        String ip = clientIp(httpReq);
        if (!rateLimiterService.allowIp(ip) || !rateLimiterService.allowEmail(req.email)) {
            throw new ApiException(1007, "请求过于频繁，请稍后再试");
        }
        // 防枚举：统一返回成功；仅当邮箱存在时发送
        User existing = userService.findByEmail(req.email);
        if (existing != null) {
            PasswordResetCodeService.CodeIssueResult issued = pwdCodeService.issueCode(req.email);
            emailService.sendVerificationCode(req.email, issued.code, pwdCodeService.ttlMinutes());
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", issued.requestId);
            return ApiResponse.success(data);
        }
        // 对不存在的邮箱，也返回成功结构，但不暴露是否存在的细节（不发送邮件）
        Map<String, Object> data = new HashMap<>();
        data.put("requestId", UUID.randomUUID().toString());
        return ApiResponse.success(data);
    }

    public static class PwdVerifyCodeReq {
        @NotBlank @Email public String email;
        @NotBlank public String code;
        @NotBlank public String requestId;
    }

    @PostMapping("/password/verify-code")
    @Operation(summary = "重置密码-验证验证码，发放重置会话")
    public ApiResponse<Map<String, Object>> passwordVerifyCode(@Valid @RequestBody PwdVerifyCodeReq req) {
        boolean ok = pwdCodeService.verifyAndConsume(req.email, req.code, req.requestId);
        if (!ok) throw new ApiException(1002, "验证码无效或已过期");
        String resetSession = pwdSessionService.createSession(req.email);
        Map<String, Object> data = new HashMap<>();
        data.put("resetSession", resetSession);
        return ApiResponse.success(data);
    }

    public static class PwdResetReq {
        @NotBlank public String resetSession;
        @NotBlank public String newPassword;
    }

    @PostMapping("/password/reset")
    @Operation(summary = "重置密码-提交新密码（全量吊销旧token）")
    public ApiResponse<Void> passwordReset(@Valid @RequestBody PwdResetReq req) {
        String email = pwdSessionService.consumeSession(req.resetSession);
        if (email == null) throw new ApiException(1003, "重置会话无效或已过期");

        // 更新密码（如用户不存在，仍返回成功以防枚举）
        User user = userService.findByEmail(email);
        if (user != null) {
            userService.resetPasswordByEmail(email, req.newPassword);
            // 全量吊销：设置 invalidAfter=now，令先前 access/refresh 全部失效
            tokenRevocationService.revokeAll(user.getUuid());
        }
        return ApiResponse.success(null);
    }

    private Map<String, Object> userView(User user) {
        Map<String, Object> u = new HashMap<>();
        u.put("uuid", user.getUuid());
        u.put("name", user.getName());
        u.put("email", user.getEmail());
        u.put("avatar", user.getAvatar());
        u.put("role", user.getRole());
        u.put("status", user.getStatus());
        u.put("company", user.getCompany());
        u.put("location", user.getLocation());
        u.put("website", user.getWebsite());
        u.put("realName", user.getRealName());
        u.put("createDate", user.getCreateDate());
        u.put("updateDate", user.getUpdateDate());
        return u;
    }

    private User withoutPassword(User user) {
        user.setPassword(null);
        return user;
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = req.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return req.getRemoteAddr();
    }
}
