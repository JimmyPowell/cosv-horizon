package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tech.cspioneer.backend.entity.OriginalLogin;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.enums.UserRole;
import tech.cspioneer.backend.enums.UserStatus;
import tech.cspioneer.backend.mapper.OriginalLoginMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.service.JwtService;
import tech.cspioneer.backend.service.UserService;
import tech.cspioneer.backend.util.OAuthStateSigner;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/oauth")
@Tag(name = "第三方认证")
@Slf4j
public class OAuthController {

    @Value("${oauth.github.client-id:}")
    private String githubClientId;
    @Value("${oauth.github.client-secret:}")
    private String githubClientSecret;
    @Value("${oauth.github.redirect-uri:}")
    private String githubRedirectUri;

    @Value("${oauth.github.scopes:read:user,user:email}")
    private String githubScopes;

    @Value("${oauth.frontend.finish-uri:}")
    private String defaultFinishUri;

    @Value("${security.jwt.secret}")
    private String stateSecret; // 直接复用 JWT secret 进行 state 签名

    private final UserService userService;
    private final UserMapper userMapper;
    private final OriginalLoginMapper originalLoginMapper;
    private final JwtService jwtService;

    public OAuthController(UserService userService,
                           UserMapper userMapper,
                           OriginalLoginMapper originalLoginMapper,
                           JwtService jwtService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.originalLoginMapper = originalLoginMapper;
        this.jwtService = jwtService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthRequest githubRequest() {
        return new AuthGithubRequest(AuthConfig.builder()
                .clientId(githubClientId)
                .clientSecret(githubClientSecret)
                .redirectUri(githubRedirectUri)
                .scopes(scopesList())
                .build());
    }

    @GetMapping("/github/render")
    @Operation(summary = "跳转 GitHub 授权页")
    public void renderGithub(@RequestParam(value = "redirect", required = false) String redirect,
                             @RequestParam(value = "bind", required = false, defaultValue = "false") boolean bind,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        AuthRequest req = githubRequest();
        // 将回跳信息编码进 state，并进行签名
        String nonce = UUID.randomUUID().toString();
        String finishUrl = (redirect == null || redirect.isBlank()) ? defaultFinishUri : redirect;

        if (bind) {
            String currentUuid = resolveCurrentUserUuidFromRequest(request);
            if (currentUuid == null || currentUuid.isBlank() || "anonymousUser".equalsIgnoreCase(currentUuid)) {
                // 未登录不能发起绑定
                String loc = (finishUrl == null || finishUrl.isBlank()) ? "/swagger-ui.html" : finishUrl;
                redirectError(response, loc, "UNAUTHORIZED", "请先登录后再进行绑定操作");
                return;
            }
            ObjectNode node = objectMapper.createObjectNode();
            node.put("nonce", nonce);
            node.put("redirect", safe(finishUrl));
            node.put("bind", true);
            node.put("uuid", currentUuid);
            String state = OAuthStateSigner.encode(node.toString(), stateSecret);
            String url = req.authorize(state);
            url = ensureGithubScopes(url);
            log.info("[OAuth] Render GitHub bind flow: uuid={} redirect={} scope={} ", currentUuid, finishUrl, extractScopeFromUrl(url));
            response.sendRedirect(url);
            return;
        }

        ObjectNode node = objectMapper.createObjectNode();
        node.put("nonce", nonce);
        node.put("redirect", safe(finishUrl));
        String state = OAuthStateSigner.encode(node.toString(), stateSecret);
        String url = req.authorize(state);
        url = ensureGithubScopes(url);
        log.info("[OAuth] Render GitHub login flow: redirect={} scope={}", finishUrl, extractScopeFromUrl(url));
        response.sendRedirect(url);
    }

    @GetMapping("/github/callback")
    @Operation(summary = "GitHub 回调处理")
    public void callbackGithub(AuthCallback callback, @RequestParam("state") String state,
                               HttpServletResponse response) throws IOException {
        State s = parseState(state);
        String finishUrl = (s.redirect == null || s.redirect.isBlank()) ? ((defaultFinishUri == null || defaultFinishUri.isBlank()) ? "/swagger-ui.html" : defaultFinishUri) : s.redirect;

        try {
            log.info("[OAuth] Callback received: bind={} redirect={}", String.valueOf(s.bind), finishUrl);
            AuthRequest req = githubRequest();
            AuthResponse<AuthUser> resp = req.login(callback);
            if (!resp.ok()) {
                log.warn("GitHub OAuth failed: code={} msg={}", resp.getCode(), resp.getMsg());
                redirectError(response, finishUrl, "OAUTH_FAILED", "GitHub 授权失败，请重试");
                return;
            }
            AuthUser gh = resp.getData();
            String ghLogin = gh.getUsername();
            log.info("[OAuth] GitHub user: login={} id={} hasToken={}", ghLogin, gh.getUuid(), gh.getToken() != null && gh.getToken().getAccessToken() != null);
            String ghEmail = resolveGithubEmail(gh);
            log.info("[OAuth] Resolved email for {} => {}", ghLogin, maskEmail(ghEmail));

            // 绑定流程：当前请求是绑定，而非登录
            if (Boolean.TRUE.equals(s.bind)) {
                if (s.uuid == null || s.uuid.isBlank()) {
                    redirectError(response, finishUrl, "STATE_INVALID", "绑定请求无效，请重试");
                    return;
                }
                User current = userService.requireUserByUuid(s.uuid);
                if (current == null || current.getStatus() != UserStatus.ACTIVE) {
                    redirectError(response, finishUrl, "USER_INVALID", "用户状态异常");
                    return;
                }
                OriginalLogin existedByName = originalLoginMapper.findBySourceAndName("GITHUB", ghLogin);
                OriginalLogin existedByUser = originalLoginMapper.findByUserIdAndSource(current.getId(), "GITHUB");

                // 若当前用户已经绑定过某个 GitHub 账号
                if (existedByUser != null) {
                    if (existedByName != null && !current.getId().equals(existedByName.getUserId())) {
                        // 想绑定的这个 GitHub 已被其他用户占用
                        redirectError(response, finishUrl + (finishUrl.contains("?")?"&":"?") + "bind=1", "BIND_CONFLICT", "该 GitHub 账号已绑定其他用户");
                        return;
                    }
                    if (existedByUser.getName().equals(ghLogin)) {
                        // 已绑定同一个 GitHub，视为成功并做邮箱升级
                        tryUpgradeEmail(current, ghEmail);
                        response.sendRedirect(finishUrl + (finishUrl.contains("?")?"&":"?") + "bind=1&success=1");
                        return;
                    }
                    // 已绑定其他 GitHub，不允许再次绑定
                    redirectError(response, finishUrl + (finishUrl.contains("?")?"&":"?") + "bind=1", "ALREADY_BOUND_OTHER", "当前账号已绑定另一个 GitHub 账号");
                    return;
                }

                if (existedByName != null && !current.getId().equals(existedByName.getUserId())) {
                    // 当前用户未绑定，但目标 GitHub 已绑定到其他用户
                    redirectError(response, finishUrl + (finishUrl.contains("?")?"&":"?") + "bind=1", "BIND_CONFLICT", "该 GitHub 账号已绑定其他用户");
                    return;
                }
                // 写入绑定关系
                OriginalLogin ol = new OriginalLogin();
                ol.setUuid(UUID.randomUUID().toString());
                ol.setUserId(current.getId());
                ol.setSource("GITHUB");
                ol.setName(ghLogin);
                originalLoginMapper.insert(ol);
                // 同步部分资料（非强制）
                String ghLink = (gh.getUsername() != null ? "https://github.com/" + gh.getUsername() : null);
                userMapper.updateProfileByUuid(current.getUuid(), gh.getAvatar(), current.getCompany(), gh.getLocation(), ghLink, gh.getBlog(), null, gh.getNickname());
                tryUpgradeEmail(current, ghEmail);
                response.sendRedirect(finishUrl + (finishUrl.contains("?")?"&":"?") + "bind=1&success=1");
                return;
            }

            // 1) 先看是否已有绑定（登录流程）
            OriginalLogin existed = originalLoginMapper.findBySourceAndName("GITHUB", ghLogin);
            User user;
            if (existed != null) {
                user = userMapper.findByUuid(userMapper.findUuidById(existed.getUserId()));
                if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                    redirectError(response, finishUrl, "USER_INVALID", "用户状态异常");
                    return;
                }
                // 已绑定用户，尝试升级邮箱
                tryUpgradeEmail(user, ghEmail);
            } else {
                // 2) 未绑定：通过邮箱判断是否存在本地账号
                if (ghEmail != null && !ghEmail.isBlank()) {
                    User localByEmail = userService.findByEmail(ghEmail);
                    if (localByEmail != null) {
                        // 若该本地账号是通过邮箱密码注册（存在密码），按需求：提示并拒绝本次 GitHub 登录
                        if (localByEmail.getPassword() != null && !localByEmail.getPassword().isBlank()) {
                            log.info("Reject GitHub login due to existing email/password account email={}", ghEmail);
                            redirectError(response, finishUrl, "EMAIL_ACCOUNT_EXISTS", "该邮箱已注册本地账号，请使用邮箱密码登录后再绑定 GitHub");
                            return;
                        }
                        // 否则允许绑定到该账号（例如此前也是第三方注册，无本地密码）
                        OriginalLogin ol = new OriginalLogin();
                        ol.setUuid(UUID.randomUUID().toString());
                        ol.setUserId(localByEmail.getId());
                        ol.setSource("GITHUB");
                        ol.setName(ghLogin);
                        originalLoginMapper.insert(ol);
                        // 已有本地账号（无密码），直接使用；若其邮箱仍为 noreply，尝试升级
                        tryUpgradeEmail(localByEmail, ghEmail);
                        user = userMapper.findByUuid(localByEmail.getUuid());
                    } else {
                        // 3) 新建用户并绑定
                        user = createUserFromGithub(gh);
                    }
                } else {
                    // 无邮箱的情况：直接新建用户（使用 noreply 邮箱）并绑定
                    user = createUserFromGithub(gh);
                }
            }

            // 签发 JWT 并回跳前端
            String access = jwtService.generateAccessToken(user.getUuid(), user.getRole().name());
            String refresh = jwtService.generateRefreshToken(user.getUuid(), user.getRole().name(), null);
            String location = finishUrl + "#accessToken=" + url(access) + "&refreshToken=" + url(refresh) + "&redirect=" + url("/dashboard");
            log.info("[OAuth] Login success: userUuid={} login={} redirecting to finish page", user.getUuid(), ghLogin);
            response.sendRedirect(location);
        } catch (Exception e) {
            log.error("GitHub OAuth callback error", e);
            redirectError(response, finishUrl, "OAUTH_EXCEPTION", "服务器处理异常，请稍后重试");
        }
    }

    private User createUserFromGithub(AuthUser gh) {
        String ghLogin = gh.getUsername();
        String name = ghLogin;
        User existedByName = userService.findByName(name);
        if (existedByName != null) {
            // 清洗为安全用户名：仅字母/数字/下划线/连字符
            name = ("gh_" + gh.getUuid()).replaceAll("[^A-Za-z0-9_-]", "_");
        }
        String email = resolveGithubEmail(gh);

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(null); // 第三方用户无本地密码
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setAvatar(gh.getAvatar());
        user.setCompany(gh.getCompany());
        user.setLocation(gh.getLocation());
        user.setWebsite(gh.getBlog());
        user.setRealName(gh.getNickname());
        user.setGitHub(gh.getUsername() != null ? "https://github.com/" + gh.getUsername() : null);
        userMapper.insert(user);

        OriginalLogin ol = new OriginalLogin();
        ol.setUuid(UUID.randomUUID().toString());
        ol.setUserId(user.getId());
        ol.setSource("GITHUB");
        ol.setName(ghLogin);
        originalLoginMapper.insert(ol);

        return userMapper.findByUuid(user.getUuid());
    }

    private void tryUpgradeEmail(User user, String newEmail) {
        try {
            if (user == null) return;
            String current = user.getEmail();
            if (current == null) return;
            if (!isNoReplyEmail(current)) return;
            if (newEmail == null || newEmail.isBlank() || isNoReplyEmail(newEmail)) return;
            User existed = userService.findByEmail(newEmail);
            if (existed != null && !existed.getUuid().equals(user.getUuid())) {
                log.info("[OAuth] Email upgrade skipped due to conflict new={} currentUser={}", maskEmail(newEmail), user.getUuid());
                return;
            }
            int n = userMapper.updateEmailByUuid(user.getUuid(), newEmail);
            if (n == 1) {
                log.info("[OAuth] Email upgraded for userUuid={} {} -> {}", user.getUuid(), maskEmail(current), maskEmail(newEmail));
            }
        } catch (Exception e) {
            log.warn("[OAuth] Email upgrade failed userUuid={} newEmail={} err=", user != null ? user.getUuid() : "?", maskEmail(newEmail), e);
        }
    }

    private State parseState(String state) {
        State s = new State();
        if (state == null || state.isBlank()) return s;
        try {
            String json = OAuthStateSigner.decode(state, stateSecret);
            if (json == null || json.isBlank()) return s;
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
            if (node.has("redirect")) s.redirect = node.get("redirect").asText();
            if (node.has("bind")) s.bind = node.get("bind").asBoolean();
            if (node.has("uuid")) s.uuid = node.get("uuid").asText();
        } catch (Exception ignored) {}
        return s;
    }

    private static class State {
        public String redirect;
        public Boolean bind;
        public String uuid;
    }

    private void redirectError(HttpServletResponse response, String finishUrl, String code, String message) throws IOException {
        String location = finishUrl + (finishUrl.contains("#") ? "&" : "#") + "error=" + url(code) + "&message=" + url(message);
        response.sendRedirect(location);
    }

    private String url(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    private String safe(String s) {
        if (s == null) return "";
        // 简单避免注入：不允许包含换行
        return s.replaceAll("[\\r\\n]", "");
    }

    private String ensureGithubScopes(String authUrl) {
        if (authUrl == null || authUrl.isBlank()) return authUrl;
        String want = String.join(" ", scopesList());
        if (!authUrl.contains("scope=")) {
            String glue = authUrl.contains("?") ? "&" : "?";
            return authUrl + glue + "scope=" + url(want);
        }
        // already has scope, ensure it contains user:email
        String lower = authUrl.toLowerCase(Locale.ROOT);
        if (lower.contains("user:email") || lower.contains("user%3aemail")) return authUrl;
        int idx = lower.indexOf("scope=");
        int end = authUrl.indexOf('&', idx);
        if (end < 0) end = authUrl.length();
        String prefix = authUrl.substring(0, end);
        String suffix = authUrl.substring(end);
        return prefix + "%20user:email" + suffix;
    }

    private List<String> scopesList() {
        if (githubScopes == null || githubScopes.isBlank()) return Arrays.asList("read:user", "user:email");
        String s = githubScopes.replace(',', ' ');
        String[] parts = s.split("\\s+");
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) set.add(t);
        }
        if (!set.contains("user:email")) set.add("user:email");
        return new ArrayList<>(set);
    }

    private String extractScopeFromUrl(String url) {
        if (url == null) return "";
        int idx = url.indexOf("scope=");
        if (idx < 0) return "";
        int end = url.indexOf('&', idx);
        if (end < 0) end = url.length();
        String raw = url.substring(idx + 6, end);
        try { return java.net.URLDecoder.decode(raw, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return raw; }
    }

    private String resolveGithubEmail(AuthUser gh) {
        try {
            String email = gh.getEmail();
            log.info("[OAuth] Initial email from profile: {} (masked)", maskEmail(email));
            if (email != null && !email.isBlank() && !isNoReplyEmail(email)) return email;
            if (gh.getToken() != null && gh.getToken().getAccessToken() != null) {
                String fetched = fetchGithubPrimaryEmail(gh.getToken().getAccessToken());
                if (fetched != null && !fetched.isBlank()) return fetched;
            }
        } catch (Exception ignored) {}
        String id = gh.getUuid() != null ? gh.getUuid() : "unknown";
        return "github_" + id + "@users.noreply.github.com";
    }

    private boolean isNoReplyEmail(String email) {
        String e = email.toLowerCase();
        return e.endsWith("@users.noreply.github.com") || e.contains("noreply.github.com");
    }

    private String fetchGithubPrimaryEmail(String accessToken) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user/emails"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[OAuth] /user/emails status={} len={}", resp.statusCode(), resp.body() != null ? resp.body().length() : -1);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                com.fasterxml.jackson.databind.JsonNode arr = objectMapper.readTree(resp.body());
                if (arr.isArray()) {
                    String candidate = null;
                    int total = 0;
                    for (com.fasterxml.jackson.databind.JsonNode n : arr) {
                        boolean verified = n.path("verified").asBoolean(false);
                        boolean primary = n.path("primary").asBoolean(false);
                        String email = n.path("email").asText(null);
                        if (email == null || email.isBlank()) continue;
                        total++;
                        if (primary && verified) return email;
                        if (verified && candidate == null) candidate = email;
                    }
                    log.info("[OAuth] /user/emails parsed total={} candidate(masked)={}", total, maskEmail(candidate));
                    if (candidate != null) return candidate;
                    for (com.fasterxml.jackson.databind.JsonNode n : arr) {
                        String email = n.path("email").asText(null);
                        if (email != null && !email.isBlank()) return email;
                    }
                }
            } else {
                log.warn("GitHub /user/emails request failed status={} body={}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.warn("GitHub /user/emails fetch error", e);
        }
        return null;
    }

    private String resolveCurrentUserUuidFromRequest(HttpServletRequest request) {
        try {
            // 1) Authorization header Bearer
            String authz = request.getHeader("Authorization");
            if (authz != null && authz.startsWith("Bearer ")) {
                String token = authz.substring(7).trim();
                JwtService.TokenInfo info = jwtService.parseAccessToken(token);
                return info.getSubjectUuid();
            }
            // 2) at query param (fallback for browser redirect that cannot set headers)
            String at = request.getParameter("at");
            if (at != null && !at.isBlank()) {
                JwtService.TokenInfo info = jwtService.parseAccessToken(at);
                return info.getSubjectUuid();
            }
            // 3) SecurityContext (may be anonymous)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) return auth.getName();
        } catch (Exception e) {
            log.warn("[OAuth] resolveCurrentUserUuidFromRequest failed", e);
        }
        return null;
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(0, at));
        return email.charAt(0) + "***" + email.substring(at);
    }
}
