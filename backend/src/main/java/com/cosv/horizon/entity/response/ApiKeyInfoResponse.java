package com.cosv.horizon.entity.response;

import java.util.Date;
import java.util.List;

/**
 * API密钥信息响应
 */
public class ApiKeyInfoResponse {
    private Long id;
    private String keyPrefix;       // 密钥前缀
    private Long creatorUserId;     // 创建者ID
    private Long organizationId;    // 关联组织ID (可能为null)
    private String description;       // 描述
    private List<String> scopes;          // 权限范围
    private String status;          // 状态
    private Date lastUsedTime;      // 最后使用时间
    private String lastUsedIp;        // 最后使用IP
    private Date expireTime;        // 过期时间
    private Date createTime;        // 创建时间

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(Date lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getLastUsedIp() {
        return lastUsedIp;
    }

    public void setLastUsedIp(String lastUsedIp) {
        this.lastUsedIp = lastUsedIp;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
} 