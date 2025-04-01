package com.cosv.horizon.entity;

import java.util.Date;
import java.util.List;

/**
 * API密钥实体类
 */
public class ApiKey {
    private Long id;
    private String keyPrefix;       // 密钥前缀，用于识别
    private String keyHash;         // 密钥的SHA-256哈希值
    private Long creatorUserId;     // 创建密钥的用户ID
    private Long organizationId;    // 关联的组织ID，NULL表示个人密钥(PAT)
    private String description;       // 用户提供的密钥描述
    private String scopes;          // 授权范围列表，逗号分隔
    private String status;          // 密钥状态: ACTIVE, REVOKED, EXPIRED
    private Date lastUsedTime;      // 最后成功使用时间
    private String lastUsedIp;        // 最后成功使用的IP地址
    private Date expireTime;        // 密钥过期时间，NULL表示永不过期
    private Date createTime;        // 创建时间
    private Date updateTime;        // 最后更新时间

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

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
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

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
} 