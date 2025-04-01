package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.ApiKey;
import com.cosv.horizon.entity.ApiKeyStatus;
import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.UserOrganizationLink;
import com.cosv.horizon.entity.request.CreateApiKeyRequest;
import com.cosv.horizon.entity.response.ApiKeyInfoResponse;
import com.cosv.horizon.mapper.ApiKeyMapper;
import com.cosv.horizon.mapper.OrganizationMapper;
import com.cosv.horizon.mapper.UserOrganizationLinkMapper;
import com.cosv.horizon.service.ApiKeyService;
import com.cosv.horizon.utils.HashingUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyServiceImpl.class);
    private static final int API_KEY_LENGTH = 40;
    private static final int KEY_PREFIX_LENGTH = 8;
    private static final String API_KEY_PREFIX = "cosv_"; // 自定义前缀

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private UserOrganizationLinkMapper userOrganizationLinkMapper;

    @Override
    @Transactional
    public ApiKey createApiKey(CreateApiKeyRequest request, Long userId) {
        // 1. 生成密钥明文
        String plainTextKey = generateUniqueApiKey();

        // 2. 计算哈希值和前缀
        String keyHash;
        try {
            keyHash = HashingUtils.sha256(plainTextKey);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not found", e);
            throw new RuntimeException("无法生成API密钥哈希值");
        }
        String keyPrefix = plainTextKey.substring(0, KEY_PREFIX_LENGTH);

        // 3. 验证权限范围 (此处简化，实际应校验用户是否有权赋予这些scopes)
        List<String> validatedScopes = validateAndFilterScopes(request.getScopes(), userId, request.getOrganizationId());

        // 4. 处理过期时间
        Date expireDate = parseExpireTime(request.getExpireTime());

        // 5. 构建ApiKey实体
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setKeyHash(keyHash);
        apiKey.setCreatorUserId(userId);
        apiKey.setOrganizationId(request.getOrganizationId());
        apiKey.setDescription(request.getDescription());
        apiKey.setScopes(String.join(",", validatedScopes)); // 逗号分隔存储
        apiKey.setStatus(ApiKeyStatus.ACTIVE.name()); // 简化，默认直接激活
        apiKey.setExpireTime(expireDate);
        apiKey.setCreateTime(new Date());
        apiKey.setUpdateTime(new Date());

        // 6. 保存到数据库
        apiKeyMapper.insert(apiKey);

        logger.info("API Key created: userId={}, orgId={}, prefix={}, scopes={}",
                userId, request.getOrganizationId(), keyPrefix, apiKey.getScopes());

        // 7. 返回包含明文密钥的实体 (仅用于此次响应)
        ApiKey responseKey = new ApiKey(); // 创建新对象以避免修改持久化实体
        responseKey = apiKey; // 复制属性
        responseKey.setKeyHash(plainTextKey); // **临时将明文放在hash字段返回**

        return responseKey;
    }

    @Override
    public ApiKey validateApiKey(String plainTextKey) {
        if (plainTextKey == null || plainTextKey.isEmpty()) {
            return null;
        }

        String keyHash;
        try {
            keyHash = HashingUtils.sha256(plainTextKey);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 calculation failed during validation", e);
            return null; // 哈希失败视为无效
        }

        ApiKey apiKey = apiKeyMapper.findActiveByKeyHash(keyHash);

        if (apiKey != null) {
            // 检查过期时间 (数据库查询已包含部分检查，这里双重确认)
            if (apiKey.getExpireTime() != null && apiKey.getExpireTime().before(new Date())) {
                logger.warn("API Key expired: id={}, prefix={}", apiKey.getId(), apiKey.getKeyPrefix());
                // 可选：更新状态为EXPIRED
                // apiKeyMapper.updateStatus(apiKey.getId(), ApiKeyStatus.EXPIRED.name());
                return null;
            }
            // 密钥有效
            return apiKey;
        } else {
            // 密钥无效或不存在
            return null;
        }
    }

    @Override
    public boolean hasScope(ApiKey apiKey, String requiredScope) {
        if (apiKey == null || apiKey.getScopes() == null || apiKey.getScopes().isEmpty() || requiredScope == null) {
            return false;
        }
        // 简单的逗号分隔检查
        List<String> grantedScopes = Arrays.asList(apiKey.getScopes().split(","));
        return grantedScopes.contains(requiredScope);
    }

    @Override
    public List<ApiKeyInfoResponse> getUserApiKeys(Long userId) {
        List<ApiKey> apiKeys = apiKeyMapper.findByCreatorUserId(userId);
        return convertToInfoResponseList(apiKeys);
    }

    @Override
    public List<ApiKeyInfoResponse> getOrganizationApiKeys(Long organizationId, Long requestingUserId) {
        // 权限检查：只有组织管理员可以查看组织的API Key
        if (!isOrganizationAdmin(organizationId, requestingUserId)) {
            logger.warn("User {} attempted to view API keys for org {} without permission", requestingUserId, organizationId);
            throw new SecurityException("您没有权限查看该组织的API密钥");
        }
        List<ApiKey> apiKeys = apiKeyMapper.findByOrganizationId(organizationId);
        return convertToInfoResponseList(apiKeys);
    }

    @Override
    public ApiKeyInfoResponse getApiKeyInfo(Long id, Long userId) {
        ApiKey apiKey = apiKeyMapper.findById(id);
        if (apiKey == null) {
            return null;
        }
        // 权限检查：用户只能查看自己创建的，或所属组织管理员可查看组织的
        boolean canView = apiKey.getCreatorUserId().equals(userId) ||
                          (apiKey.getOrganizationId() != null && isOrganizationAdmin(apiKey.getOrganizationId(), userId));

        if (!canView) {
            logger.warn("User {} attempted to view API key {} without permission", userId, id);
            throw new SecurityException("您没有权限查看此API密钥");
        }
        return convertToInfoResponse(apiKey);
    }

    @Override
    @Transactional
    public boolean revokeApiKey(Long id, Long userId) {
        ApiKey apiKey = apiKeyMapper.findById(id);
        if (apiKey == null) {
            logger.warn("Attempted to revoke non-existent API key: id={}", id);
            return false; // 或者抛出异常
        }

        // 权限检查：用户只能撤销自己创建的，或所属组织管理员可撤销组织的
        boolean canRevoke = apiKey.getCreatorUserId().equals(userId) ||
                            (apiKey.getOrganizationId() != null && isOrganizationAdmin(apiKey.getOrganizationId(), userId));

        if (!canRevoke) {
            logger.warn("User {} attempted to revoke API key {} without permission", userId, id);
            throw new SecurityException("您没有权限撤销此API密钥");
        }

        if (!ApiKeyStatus.ACTIVE.name().equals(apiKey.getStatus())) {
            logger.info("API key {} already revoked or expired.", id);
            return true; // 已经是无效状态，视为成功
        }

        int result = apiKeyMapper.revokeById(id);
        logger.info("API Key revoked: id={}, userId={}", id, userId);
        return result > 0;
    }

    @Override
    public void updateLastUsed(Long apiKeyId, String ipAddress) {
        try {
            apiKeyMapper.updateLastUsed(apiKeyId, new Date(), ipAddress);
        } catch (Exception e) {
            // 记录错误，但不应阻塞主流程
            logger.error("Failed to update last used info for API key: id={}", apiKeyId, e);
        }
    }

    // --- Helper Methods ---

    private String generateUniqueApiKey() {
        // 生成包含前缀的总长度为API_KEY_LENGTH + PREFIX_LENGTH 的key
        // 确保前缀唯一性，并增加随机性
        String key = API_KEY_PREFIX + RandomStringUtils.randomAlphanumeric(API_KEY_LENGTH);
        // 实际应用中可能需要检查数据库确保唯一性，但对于足够长的随机密钥，碰撞概率极低
        return key;
    }

    private List<String> validateAndFilterScopes(List<String> requestedScopes, Long userId, Long organizationId) {
        // TODO: 实现实际的权限范围验证逻辑
        // 1. 定义所有可用的 scopes
        // 2. 检查用户是否有权为自己或组织授予请求的 scopes
        // 3. 如果是组织key，检查用户在该组织的角色，确定可授予的最大范围
        // 4. 过滤掉无效或用户无权授予的 scopes
        logger.warn("Scope validation not fully implemented. Returning requested scopes for now.");
        return requestedScopes == null ? Collections.emptyList() : requestedScopes;
    }

    private Date parseExpireTime(String expireTimeString) {
        if (expireTimeString == null || expireTimeString.trim().isEmpty()) {
            return null; // 永不过期
        }
        try {
            // 尝试解析 ISO 8601 格式
            return Date.from(Instant.parse(expireTimeString));
        } catch (Exception e) {
            // 尝试解析描述性字符串，如 "30d", "1y"
            try {
                long daysToAdd = 0;
                if (expireTimeString.endsWith("d")) {
                    daysToAdd = Long.parseLong(expireTimeString.substring(0, expireTimeString.length() - 1));
                } else if (expireTimeString.endsWith("m")) {
                    daysToAdd = Long.parseLong(expireTimeString.substring(0, expireTimeString.length() - 1)) * 30; // 简化
                } else if (expireTimeString.endsWith("y")) {
                    daysToAdd = Long.parseLong(expireTimeString.substring(0, expireTimeString.length() - 1)) * 365; // 简化
                }
                if (daysToAdd > 0) {
                    return Date.from(Instant.now().plus(Duration.ofDays(daysToAdd)));
                }
            } catch (NumberFormatException ex) {
                logger.warn("Invalid expire time format: {}", expireTimeString);
            }
        }
        return null; // 解析失败，视为永不过期
    }

    private boolean isOrganizationAdmin(Long organizationId, Long userId) {
        if (organizationId == null || userId == null) {
            return false;
        }
        UserOrganizationLink link = userOrganizationLinkMapper.findByOrganizationAndUser(organizationId, userId);
        return link != null && link.hasAdminPrivilege(); // 使用 UserOrganizationLink 中的方法
    }

    @Override
    public ApiKeyInfoResponse convertToInfoResponse(ApiKey apiKey) {
        if (apiKey == null) {
            return null;
        }
        ApiKeyInfoResponse dto = new ApiKeyInfoResponse();
        dto.setId(apiKey.getId());
        dto.setKeyPrefix(apiKey.getKeyPrefix());
        dto.setCreatorUserId(apiKey.getCreatorUserId());
        dto.setOrganizationId(apiKey.getOrganizationId());
        dto.setDescription(apiKey.getDescription());
        dto.setScopes(apiKey.getScopes() == null ? Collections.emptyList() : Arrays.asList(apiKey.getScopes().split(",")));
        dto.setStatus(apiKey.getStatus());
        dto.setLastUsedTime(apiKey.getLastUsedTime());
        dto.setLastUsedIp(apiKey.getLastUsedIp());
        dto.setExpireTime(apiKey.getExpireTime());
        dto.setCreateTime(apiKey.getCreateTime());
        return dto;
    }

    @Override
    public List<ApiKeyInfoResponse> convertToInfoResponseList(List<ApiKey> apiKeys) {
        if (CollectionUtils.isEmpty(apiKeys)) {
            return Collections.emptyList();
        }
        return apiKeys.stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());
    }
} 