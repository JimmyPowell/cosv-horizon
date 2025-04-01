package com.cosv.horizon.entity.request;

import java.util.List;

/**
 * 创建API密钥请求
 */
public class CreateApiKeyRequest {
    private Long organizationId; // 如果是组织密钥，则提供组织ID，否则为个人密钥
    private String description;    // 密钥描述
    private List<String> scopes;     // 请求的权限范围
    private String expireTime;     // 过期时间（可选，ISO 8601格式或描述性字符串如"30d"）

    // Getters and Setters
    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
} 