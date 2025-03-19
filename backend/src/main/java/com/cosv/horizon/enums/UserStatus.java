package com.cosv.horizon.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    /**
     * 正常状态
     */
    NORMAL("正常"),
    
    /**
     * 待认证状态
     */
    PENDING("待认证"),
    
    /**
     * 已删除状态
     */
    DELETED("删除"),
    
    /**
     * 封禁状态
     */
    BANNED("封禁");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 