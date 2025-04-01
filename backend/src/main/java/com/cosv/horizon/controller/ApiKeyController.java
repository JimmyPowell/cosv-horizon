package com.cosv.horizon.controller;

import com.cosv.horizon.entity.ApiKey;
import com.cosv.horizon.entity.Constans;
import com.cosv.horizon.entity.HttpResponseEntity;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.CreateApiKeyRequest;
import com.cosv.horizon.entity.response.ApiKeyInfoResponse;
import com.cosv.horizon.service.ApiKeyService;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API密钥管理控制器
 */
@RestController
@RequestMapping("/api/apikeys")
public class ApiKeyController {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyController.class);

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private UserService userService; // 用于获取用户信息

    /**
     * 创建API密钥
     *
     * @param request 创建请求
     * @return 包含明文密钥的创建结果 (明文仅此一次返回)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createApiKey(@RequestBody CreateApiKeyRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ApiKey createdKey = apiKeyService.createApiKey(request, userId);
            
            // 准备响应，包含明文key
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdKey.getId());
            response.put("plainTextKey", createdKey.getKeyHash()); // 临时用keyHash字段传递明文
            response.put("keyPrefix", createdKey.getKeyPrefix());
            response.put("description", createdKey.getDescription());
            response.put("scopes", createdKey.getScopes());
            response.put("expireTime", createdKey.getExpireTime());
            response.put("organizationId", createdKey.getOrganizationId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException se) {
             logger.warn("创建API密钥权限不足: userId={}", userId, se);
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("message", se.getMessage());
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.error("创建API密钥失败: userId={}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "创建API密钥失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取当前用户的所有API密钥信息
     *
     * @return API密钥列表
     */
    @GetMapping("/my")
    public ResponseEntity<List<ApiKeyInfoResponse>> getCurrentUserApiKeys() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ApiKeyInfoResponse> keys = apiKeyService.getUserApiKeys(userId);
            return ResponseEntity.ok(keys);
        } catch (Exception e) {
            logger.error("获取用户API密钥失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取组织的API密钥信息 (需要组织管理员权限)
     *
     * @param organizationId 组织ID
     * @return API密钥列表
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<ApiKeyInfoResponse>> getOrganizationApiKeys(@PathVariable Long organizationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ApiKeyInfoResponse> keys = apiKeyService.getOrganizationApiKeys(organizationId, userId);
            return ResponseEntity.ok(keys);
        } catch (SecurityException se) {
            logger.warn("获取组织API密钥权限不足: userId={}, orgId={}", userId, organizationId, se);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("获取组织API密钥失败: orgId={}", organizationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取单个API密钥信息
     *
     * @param id 密钥ID
     * @return API密钥信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyInfoResponse> getApiKeyInfo(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ApiKeyInfoResponse keyInfo = apiKeyService.getApiKeyInfo(id, userId);
            if (keyInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(keyInfo);
        } catch (SecurityException se) {
            logger.warn("获取API密钥信息权限不足: userId={}, keyId={}", userId, id, se);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("获取API密钥信息失败: keyId={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 撤销API密钥
     *
     * @param id 密钥ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            boolean success = apiKeyService.revokeApiKey(id, userId);
            if (success) {
                return ResponseEntity.noContent().build(); // 成功撤销，返回204
            } else {
                return ResponseEntity.notFound().build(); // 密钥不存在或已撤销
            }
        } catch (SecurityException se) {
            logger.warn("撤销API密钥权限不足: userId={}, keyId={}", userId, id, se);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("撤销API密钥失败: keyId={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 