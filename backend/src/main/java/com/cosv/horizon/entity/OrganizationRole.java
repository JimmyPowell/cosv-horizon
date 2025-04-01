package com.cosv.horizon.entity;

/**
 * 组织角色枚举
 * 定义用户在组织中可能的角色
 */
public enum OrganizationRole {
    /**
     * 管理员 - 拥有组织的全部管理权限
     */
    ADMIN,
    
    /**
     * 成员 - 普通组织成员
     */
    MEMBER,
    
    /**
     * 观察者 - 只有查看权限
     */
    OBSERVER,
    
    /**
     * 贡献者 - 可以提交内容但无管理权限
     */
    CONTRIBUTOR,
    
    /**
     * 访客 - 临时访问权限
     */
    GUEST;
    
    /**
     * 检查给定的角色是否有效
     * @param role 角色字符串
     * @return 是否为有效角色
     */
    public static boolean isValid(String role) {
        try {
            valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 检查角色是否具有管理权限
     * @param role 角色
     * @return 是否有管理权限
     */
    public static boolean hasAdminPrivilege(OrganizationRole role) {
        return role == ADMIN;
    }
    
    /**
     * 检查角色是否具有编辑权限
     * @param role 角色
     * @return 是否有编辑权限
     */
    public static boolean hasEditPrivilege(OrganizationRole role) {
        return role == ADMIN || role == CONTRIBUTOR;
    }
} 