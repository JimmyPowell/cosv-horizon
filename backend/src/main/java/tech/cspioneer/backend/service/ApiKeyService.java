package tech.cspioneer.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.ApiKey;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.ApiKeyMapper;
import tech.cspioneer.backend.mapper.LnkUserOrganizationMapper;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.mapper.ApiKeyUsageLogMapper;
import tech.cspioneer.backend.enums.OrganizationRole;
import tech.cspioneer.backend.enums.ApiKeyStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
public class ApiKeyService {
    private final ApiKeyMapper apiKeyMapper;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final LnkUserOrganizationMapper lnkUserOrganizationMapper;
    private final ApiKeyUsageLogMapper apiKeyUsageLogMapper;

    @Value("${apikey.prefix:cosv}")
    private String apiKeyPrefix;

    public ApiKeyService(ApiKeyMapper apiKeyMapper, UserMapper userMapper, OrganizationMapper organizationMapper, LnkUserOrganizationMapper lnkUserOrganizationMapper, ApiKeyUsageLogMapper apiKeyUsageLogMapper) {
        this.apiKeyMapper = apiKeyMapper;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.lnkUserOrganizationMapper = lnkUserOrganizationMapper;
        this.apiKeyUsageLogMapper = apiKeyUsageLogMapper;
    }

    public static class CreatedKey {
        public final ApiKey meta;
        public final String fullKey;
        public CreatedKey(ApiKey meta, String fullKey) { this.meta = meta; this.fullKey = fullKey; }
    }

    @Transactional
    public CreatedKey create(String userUuid, String organizationUuid, String description, List<String> scopes, LocalDateTime expireTime) {
        User user = requireUser(userUuid);
        Long orgId = null;
        if (organizationUuid != null && !organizationUuid.isBlank()) {
            Organization org = organizationMapper.findByUuid(organizationUuid);
            if (org == null) throw new ApiException(404, "组织不存在");
            // 仅组织管理员可创建组织级别的 API Key
            var link = lnkUserOrganizationMapper.findByOrgIdAndUserId(org.getId(), user.getId());
            if (link == null || link.getRole() != OrganizationRole.ADMIN) throw new ApiException(1012, "权限不足");
            orgId = org.getId();
        }

        String prefix = randomAlphaNum(8);
        String secret = randomAlphaNum(32);
        String fullKey = apiKeyPrefix + "_" + prefix + "_" + secret;
        String hash = sha256Hex(secret);

        ApiKey k = new ApiKey();
        k.setUuid(UUID.randomUUID().toString());
        k.setKeyPrefix(prefix);
        k.setKeyHash(hash);
        k.setCreatorUserId(user.getId());
        k.setOrganizationId(orgId);
        k.setDescription(description);
        // 规范 scopes：仅允许 ALLOWED_SCOPES；个人 Key 禁止 org:*
        String scopesStr = null;
        if (scopes != null) {
            LinkedHashSet<String> norm = new LinkedHashSet<>();
            for (String s : scopes) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty()) continue;
                if (!ALLOWED_SCOPES.contains(t)) {
                    throw new ApiException(1001, "不支持的scope: " + t);
                }
                if (orgId == null && (t.startsWith("org:"))) {
                    throw new ApiException(1012, "个人Key不允许组织管理权限");
                }
                norm.add(t);
            }
            if (!norm.isEmpty()) scopesStr = String.join(",", norm);
        }
        k.setScopes(scopesStr);
        k.setStatus(ApiKeyStatus.ACTIVE);
        k.setExpireTime(expireTime);
        apiKeyMapper.insert(k);
        // 返回明文仅一次
        return new CreatedKey(k, fullKey);
    }

    public List<ApiKey> listMine(String userUuid) {
        User user = requireUser(userUuid);
        return apiKeyMapper.listByCreator(user.getId());
    }

    public List<ApiKey> listByOrg(String userUuid, String orgUuid) {
        User user = requireUser(userUuid);
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        var link = lnkUserOrganizationMapper.findByOrgIdAndUserId(org.getId(), user.getId());
        if (link == null || link.getRole() != OrganizationRole.ADMIN) throw new ApiException(1012, "权限不足");
        return apiKeyMapper.listByOrganization(org.getId());
    }

    public void revoke(String userUuid, String keyUuid) {
        User user = requireUser(userUuid);
        ApiKey key = apiKeyMapper.findByUuid(keyUuid);
        if (key == null) throw new ApiException(404, "密钥不存在");
        boolean allowed = key.getCreatorUserId().equals(user.getId());
        if (!allowed && key.getOrganizationId() != null) {
            var link = lnkUserOrganizationMapper.findByOrgIdAndUserId(key.getOrganizationId(), user.getId());
            if (link != null && link.getRole() == OrganizationRole.ADMIN) allowed = true;
        }
        if (!allowed) throw new ApiException(1012, "权限不足");
        apiKeyMapper.updateStatus(keyUuid, ApiKeyStatus.REVOKED.name());
    }

    public static class UsagePage {
        public final java.util.List<tech.cspioneer.backend.entity.ApiKeyUsageLog> items;
        public final long total;
        public UsagePage(java.util.List<tech.cspioneer.backend.entity.ApiKeyUsageLog> items, long total) { this.items = items; this.total = total; }
    }

    public UsagePage usage(String userUuid, String keyUuid, int page, int size, String fromTs, String toTs) {
        User user = requireUser(userUuid);
        ApiKey key = apiKeyMapper.findByUuid(keyUuid);
        if (key == null) throw new ApiException(404, "密钥不存在");
        boolean allowed = key.getCreatorUserId().equals(user.getId());
        if (!allowed && key.getOrganizationId() != null) {
            var link = lnkUserOrganizationMapper.findByOrgIdAndUserId(key.getOrganizationId(), user.getId());
            if (link != null && link.getRole() == OrganizationRole.ADMIN) allowed = true;
        }
        if (!allowed) throw new ApiException(1012, "权限不足");
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        Long apiKeyId = key.getId();
        var items = apiKeyUsageLogMapper.listByApiKey(apiKeyId, emptyToNull(fromTs), emptyToNull(toTs), limit, offset);
        long total = apiKeyUsageLogMapper.countByApiKey(apiKeyId, emptyToNull(fromTs), emptyToNull(toTs));
        return new UsagePage(items, total);
    }

    private String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    public ApiKey validateAndTouch(String headerValue, String ip) {
        if (headerValue == null || headerValue.isBlank()) return null;
        String token = headerValue.trim();
        // 支持两种格式："ApiKey <token>" 或直接传 token
        if (token.toLowerCase().startsWith("apikey ")) token = token.substring(7).trim();
        String[] parts = token.split("_");
        if (parts.length < 3) return null;
        String prefix = parts[1];
        String secret = parts[2];
        String hash = sha256Hex(secret);
        ApiKey k = apiKeyMapper.findActiveByPrefixAndHash(prefix, hash);
        if (k != null) {
            apiKeyMapper.touch(k.getId(), ip);
        }
        return k;
    }

    public static class AuthContext {
        public ApiKey key;
        public String userUuid;
        public String orgUuid;
        public String subjectType; // USER or ORG
    }

    public AuthContext validateAndResolveContext(String headerValue, String ip) {
        ApiKey k = validateAndTouch(headerValue, ip);
        if (k == null) return null;
        AuthContext ctx = new AuthContext();
        ctx.key = k;
        String userUuid = userMapper.findUuidById(k.getCreatorUserId());
        ctx.userUuid = userUuid;
        if (k.getOrganizationId() != null) {
            Organization org = organizationMapper.findById(k.getOrganizationId());
            ctx.orgUuid = org == null ? null : org.getUuid();
            ctx.subjectType = "ORG";
        } else {
            ctx.subjectType = "USER";
        }
        return ctx;
    }

    private User requireUser(String uuid) {
        User u = userMapper.findByUuid(uuid);
        if (u == null) throw new ApiException(1005, "用户不存在");
        return u;
    }

    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "vuln:read", "vuln:write",
            "org:read", "org:write",
            "notification:read"
    );

    @Transactional
    public ApiKey update(String userUuid,
                         String keyUuid,
                         String description,
                         List<String> scopes,
                         String expireTimeStr) {
        User user = requireUser(userUuid);
        ApiKey key = apiKeyMapper.findByUuid(keyUuid);
        if (key == null) throw new ApiException(404, "密钥不存在");
        boolean allowed = key.getCreatorUserId().equals(user.getId());
        if (!allowed && key.getOrganizationId() != null) {
            var link = lnkUserOrganizationMapper.findByOrgIdAndUserId(key.getOrganizationId(), user.getId());
            if (link != null && link.getRole() == OrganizationRole.ADMIN) allowed = true;
        }
        if (!allowed) throw new ApiException(1012, "权限不足");

        // scopes 处理（仅当传入非 null 时更新；空数组表示清空）
        String scopesStr = null;
        boolean scopesProvided = (scopes != null);
        if (scopesProvided) {
            LinkedHashSet<String> norm = new LinkedHashSet<>();
            for (String s : scopes) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty()) continue;
                if (!ALLOWED_SCOPES.contains(t)) {
                    throw new ApiException(1001, "不支持的scope: " + t);
                }
                // 若是个人 Key，禁止 org:* 范围
                if (key.getOrganizationId() == null && t.startsWith("org:")) {
                    throw new ApiException(1012, "个人Key不允许组织管理权限");
                }
                norm.add(t);
            }
            if (!norm.isEmpty()) scopesStr = String.join(",", norm);
            else scopesStr = null; // 清空
        }

        // 过期时间处理（null=不变; 空字符串=清除; 否则解析）
        boolean expireProvided = false;
        LocalDateTime expireTime = null;
        if (expireTimeStr != null) {
            expireProvided = true;
            if (expireTimeStr.isBlank()) {
                expireTime = null; // 清除
            } else {
                expireTime = parseExpireTime(expireTimeStr);
            }
        }

        boolean descProvided = (description != null);

        if (!(descProvided || scopesProvided || expireProvided)) {
            return key; // 无更新
        }

        apiKeyMapper.updateFields(keyUuid,
                description, descProvided,
                scopesStr, scopesProvided,
                expireTime, expireProvided);

        return apiKeyMapper.findByUuid(keyUuid);
    }

    /**
     * 解析过期时间字符串：
     * - 支持带时区/UTC（Z 或 +08:00 等）：按该时刻换算为服务器本地时间存储
     * - 支持本地无时区格式（yyyy-MM-dd'T'HH:mm:ss 或 yyyy-MM-dd HH:mm:ss）：按服务器本地时间解析
     * - 校验必须大于当前时间
     */
    private LocalDateTime parseExpireTime(String raw) {
        String s = raw == null ? null : raw.trim();
        if (s == null || s.isEmpty()) return null;
        LocalDateTime dt = null;
        // 带时区/UTC
        try {
            if (s.endsWith("Z")) {
                Instant ins = Instant.parse(s);
                dt = LocalDateTime.ofInstant(ins, ZoneId.systemDefault());
            } else if (s.matches(".*[+-][0-9]{2}:?[0-9]{2}$")) {
                OffsetDateTime odt = OffsetDateTime.parse(s);
                Instant ins = odt.toInstant();
                dt = LocalDateTime.ofInstant(ins, ZoneId.systemDefault());
            }
        } catch (Exception ignore) {}
        // 本地无时区：标准 ISO LocalDateTime
        if (dt == null) {
            try { dt = LocalDateTime.parse(s); } catch (Exception ignore) {}
        }
        // 本地常用格式：yyyy-MM-dd HH:mm:ss
        if (dt == null) {
            try { dt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); } catch (Exception ignore) {}
        }
        if (dt == null) throw new ApiException(1001, "expireTime格式无效，支持ISO-8601（可带时区/UTC）或yyyy-MM-dd HH:mm:ss");
        if (!dt.isAfter(LocalDateTime.now())) throw new ApiException(1001, "expireTime 已过期或无效，应为未来时间");
        return dt;
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String randomAlphaNum(int len) {
        SecureRandom r = new SecureRandom();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}
