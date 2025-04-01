package com.cosv.horizon.controller;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.CodeVerifyRequest;
import com.cosv.horizon.entity.request.GenerateCodeRequest;
import com.cosv.horizon.entity.request.RefreshTokenRequest;
import com.cosv.horizon.entity.request.UserLoginRequest;
import com.cosv.horizon.entity.request.UserRegisterRequest;
import com.cosv.horizon.entity.request.VerifySessionRequest;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.entity.response.TokenRefreshResponse;
import com.cosv.horizon.entity.response.UserLoginResponse;
import com.cosv.horizon.entity.response.VerificationResponse;
import com.cosv.horizon.service.AuthService;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.Constans;
import com.cosv.horizon.utils.JwtUtil;
import com.cosv.horizon.utils.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * 处理用户注册、登录、令牌刷新和登出等认证相关功能
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 发送验证码到用户邮箱中，同时将对应的邮箱-验证码键值对存入redis
     * 旧版API，为了兼容性保留
     * @param request 包含email字段的请求体
     * @return 成功/失败响应
     */
    @PostMapping("generatecode")
    public ResponseEntity<HttpResponseEntity> generateCode(@RequestBody GenerateCodeRequest request) {
        try {
            boolean result = authService.generateCode(request.getEmail());
            if (result) {
                logger.info("验证码发送成功: {}", request.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE, 
                    Constans.SUCCESS_MESSAGE, 
                    "验证码发送成功"
                ));
            } else {
                logger.warn("验证码发送失败: {}", request.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE, 
                    Constans.EXIST_MESSAGE, 
                    "验证码发送失败"
                ));
            }
        } catch (Exception e) {
            logger.error("验证码发送异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "验证码发送失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 验证验证码
     * 旧版API，为了兼容性保留
     * @param codeVerifyRequest 验证码验证请求
     * @return 成功/失败响应
     */
    @PostMapping("verifycode") 
    public ResponseEntity<HttpResponseEntity> verifyCode(@RequestBody CodeVerifyRequest codeVerifyRequest) {
        try {
            boolean result = authService.validateCode(codeVerifyRequest);
            if (result) {
                logger.info("验证码验证成功: {}", codeVerifyRequest.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE, 
                    Constans.SUCCESS_MESSAGE, 
                    "验证码验证成功"
                ));
            } else {
                logger.warn("验证码验证失败: {}", codeVerifyRequest.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE, 
                    Constans.EXIST_MESSAGE, 
                    "验证码验证失败"
                ));
            }
        } catch (Exception e) {
            logger.error("验证码验证异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "验证码验证失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送验证码并创建会话
     * 新版API，用于安全的注册流程
     * @param request 包含email字段的请求体
     * @return 包含sessionId的响应
     */
    @PostMapping("send-verification-code")
    public ResponseEntity<HttpResponseEntity> sendVerificationCode(@RequestBody GenerateCodeRequest request) {
        try {
            String sessionId = authService.sendVerificationCode(request.getEmail());
            if (sessionId != null) {
                logger.info("验证码发送成功并创建会话: {}", request.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE, 
                    new VerificationResponse(sessionId),
                    "验证码发送成功"
                ));
            } else {
                logger.warn("验证码发送或会话创建失败: {}", request.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "验证码发送失败"
                ));
            }
        } catch (Exception e) {
            logger.error("验证码发送异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "验证码发送失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证会话和验证码
     * 新版API，用于安全的注册流程
     * @param request 会话验证请求
     * @return 验证结果
     */
    @PostMapping("verify-session")
    public ResponseEntity<HttpResponseEntity> verifySession(@RequestBody VerifySessionRequest request) {
        try {
            boolean result = authService.verifySession(request);
            if (result) {
                logger.info("会话验证成功: {}", request.getSessionId());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    true,
                    "验证成功"
                ));
            } else {
                logger.warn("会话验证失败: {}", request.getSessionId());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    false,
                    "验证失败，请检查验证码是否正确"
                ));
            }
        } catch (Exception e) {
            logger.error("会话验证异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "会话验证失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 用户注册接口
     * 需要先验证邮箱，才能注册
     * @param request 用户注册请求，包含sessionId和用户信息
     * @return 注册结果
     */
    @PostMapping("register")
    public ResponseEntity<HttpResponseEntity> register(@RequestBody UserRegisterRequest request) {
        try {
            if (!SessionUtils.isSessionVerified(request.getSessionId())) {
                logger.warn("注册失败：会话未验证 {}", request.getSessionId());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "请先完成邮箱验证"
                ));
            }
            
            String email = SessionUtils.getSessionEmail(request.getSessionId());
            if (email == null) {
                logger.warn("注册失败：会话无效或已过期 {}", request.getSessionId());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "会话无效或已过期"
                ));
            }
            
            User user = new User();
            user.setName(request.getName());
            user.setPassword(request.getPassword());
            user.setEmail(email);
            user.setRole(request.getRole());
            user.setAvatar(request.getAvatar());
            user.setCompany(request.getCompany());
            user.setLocation(request.getLocation());
            user.setGitHub(request.getGitHub());
            user.setWebsite(request.getWebsite());
            user.setFreeText(request.getFreeText());
            user.setRealName(request.getRealName());
            
            boolean result = userService.register(user);
            
            if (result) {
                SessionUtils.clearSession(request.getSessionId());
                logger.info("用户注册成功: {}", user.getName());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    null,
                    "注册成功"
                ));
            } else {
                logger.warn("用户注册失败: {}", user.getName());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "注册失败，可能用户名或邮箱已存在"
                ));
            }
        } catch (Exception e) {
            logger.error("用户注册异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "注册失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 用户登录接口
     * @param request 登录请求，包含邮箱和密码
     * @return 登录结果，成功时返回JWT令牌
     */
    @PostMapping("login")
    public ResponseEntity<HttpResponseEntity> login(@RequestBody UserLoginRequest request) {
        try {
            User user = authService.login(request);
            
            if (user == null) {
                logger.warn("登录失败：邮箱或密码错误 {}", request.getEmail());
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "邮箱或密码错误"
                ));
            }
            
            String accessToken = jwtUtil.generateAccessToken(user.getName(), user.getUuid());
            String refreshToken = jwtUtil.generateRefreshToken(user.getName(), user.getUuid());
            
            UserLoginResponse loginResponse = new UserLoginResponse(accessToken, refreshToken);
            
            logger.info("用户登录成功: {}", user.getName());
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                loginResponse,
                "登录成功"
            ));
        } catch (Exception e) {
            logger.error("用户登录异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 刷新令牌
     * @param request 刷新令牌请求
     * @return 新的令牌
     */
    @PostMapping("refresh-token")
    public ResponseEntity<HttpResponseEntity> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            TokenRefreshResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
            
            if (tokenResponse == null) {
                logger.warn("令牌刷新失败");
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    Constans.TOKEN_EXPIRED_MESSAGE
                ));
            }
            
            logger.info("令牌刷新成功");
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                tokenResponse,
                "令牌刷新成功"
            ));
        } catch (Exception e) {
            logger.error("令牌刷新异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.AUTH_ERROR_CODE,
                null,
                "令牌刷新失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 用户登出
     * @param token 访问令牌
     * @return 登出结果
     */
    @PostMapping("logout")
    public ResponseEntity<HttpResponseEntity> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (!jwtUtil.validateToken(token)) {
                logger.warn("登出失败：无效的令牌");
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "无效的令牌"
                ));
            }
            
            String userUuid = jwtUtil.extractUserUuid(token);
            
            boolean result = authService.logout(userUuid);
            
            if (result) {
                logger.info("用户登出成功: {}", userUuid);
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    null,
                    "登出成功"
                ));
            } else {
                logger.warn("用户登出失败: {}", userUuid);
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "登出失败"
                ));
            }
        } catch (Exception e) {
            logger.error("用户登出异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "登出失败: " + e.getMessage()
            ));
        }
    }
}

