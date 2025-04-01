package com.cosv.horizon.entity;

/**
 * API密钥状态枚举
 */
public enum ApiKeyStatus {
    /**
     * 活跃状态
     */
    ACTIVE,
    
    /**
     * 已撤销
     */
    REVOKED,
    
    /**
     * 已过期
     */
    EXPIRED
} 