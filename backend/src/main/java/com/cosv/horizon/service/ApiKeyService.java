package com.cosv.horizon.service;

import com.cosv.horizon.entity.ApiKey;
import com.cosv.horizon.entity.request.CreateApiKeyRequest;
import com.cosv.horizon.entity.response.ApiKeyInfoResponse;

import java.util.List;

/**
 * API密钥服务接口
 */
public interface ApiKeyService {

    /**
     * 创建API密钥
     *
     * @param request 创建请求
     * @param userId 创建者用户ID
     * @return 包含明文密钥的响应对象 (明文密钥仅在创建时返回一次)
     */
    ApiKey createApiKey(CreateApiKeyRequest request, Long userId);

    /**
     * 根据明文密钥验证并获取密钥信息
     *
     * @param plainTextKey 明文密钥
     * @return 有效的API密钥实体，无效则返回null
     */
    ApiKey validateApiKey(String plainTextKey);

    /**
     * 检查API密钥是否具有指定权限范围
     *
     * @param apiKey 密钥实体
     * @param requiredScope 需要的权限
     * @return 是否具有权限
     */
    boolean hasScope(ApiKey apiKey, String requiredScope);

    /**
     * 获取用户的所有API密钥信息
     *
     * @param userId 用户ID
     * @return API密钥信息列表（不含哈希）
     */
    List<ApiKeyInfoResponse> getUserApiKeys(Long userId);

    /**
     * 获取组织的API密钥信息（仅限组织管理员查看）
     *
     * @param organizationId 组织ID
     * @param requestingUserId 请求者ID
     * @return API密钥信息列表（不含哈希）
     */
    List<ApiKeyInfoResponse> getOrganizationApiKeys(Long organizationId, Long requestingUserId);

    /**
     * 获取指定ID的API密钥信息
     *
     * @param id 密钥ID
     * @param userId 请求者用户ID (用于权限检查)
     * @return API密钥信息（不含哈希）
     */
    ApiKeyInfoResponse getApiKeyInfo(Long id, Long userId);

    /**
     * 撤销API密钥
     *
     * @param id 密钥ID
     * @param userId 操作者用户ID (用于权限检查)
     * @return 是否成功
     */
    boolean revokeApiKey(Long id, Long userId);

    /**
     * 更新密钥最后使用信息
     *
     * @param apiKeyId 密钥ID
     * @param ipAddress IP地址
     */
    void updateLastUsed(Long apiKeyId, String ipAddress);

    /**
     * 将ApiKey实体转换为ApiKeyInfoResponse DTO
     *
     * @param apiKey 实体
     * @return DTO
     */
    ApiKeyInfoResponse convertToInfoResponse(ApiKey apiKey);

    /**
     * 将ApiKey实体列表转换为ApiKeyInfoResponse DTO列表
     *
     * @param apiKeys 实体列表
     * @return DTO列表
     */
    List<ApiKeyInfoResponse> convertToInfoResponseList(List<ApiKey> apiKeys);
} 