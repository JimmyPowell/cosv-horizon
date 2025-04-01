package com.cosv.horizon.entity;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    /**
     * 系统通知
     */
    SYSTEM_NOTICE,
    
    /**
     * 组织审核通知
     */
    ORGANIZATION_REVIEW,
    
    /**
     * 组织转让请求
     */
    ORGANIZATION_TRANSFER_REQUEST,
    
    /**
     * 组织成员邀请
     */
    ORGANIZATION_MEMBER_INVITE,
    
    /**
     * 组织角色变更
     */
    ORGANIZATION_ROLE_CHANGE
} 