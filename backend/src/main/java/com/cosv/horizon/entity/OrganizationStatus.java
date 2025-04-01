package com.cosv.horizon.entity;

/**
 * 组织状态枚举
 * 定义组织可能的状态
 */
public enum OrganizationStatus {
    /**
     * 活跃状态 - 组织正常运行
     */
    ACTIVE,
    
    /**
     * 待审核状态 - 组织注册后等待审核
     */
    PENDING,
    
    /**
     * 暂停状态 - 组织暂时被禁用
     */
    SUSPENDED,
    
    /**
     * 关闭状态 - 组织已关闭
     */
    CLOSED,
    
    /**
     * 违规状态 - 组织因违反规定被标记
     */
    VIOLATED;
    
    /**
     * 检查给定的状态是否有效
     * @param status 状态字符串
     * @return 是否为有效状态
     */
    public static boolean isValid(String status) {
        try {
            valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 