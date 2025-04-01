package com.cosv.horizon.entity;

/**
 * 用户与组织的关联实体类
 * 对应数据库中的lnk_user_organization表
 */
public class UserOrganizationLink {
    private Long id;                 // 自增主键
    private Long organizationId;     // 组织ID
    private Long userId;             // 用户ID
    private String role;             // 用户在组织中的角色（如ADMIN, MEMBER等）
    
    // 额外的对象引用，不映射到数据库
    private Organization organization;
    private User user;

    // 默认构造函数
    public UserOrganizationLink() {
    }

    // 带参数的构造函数
    public UserOrganizationLink(Long organizationId, Long userId, String role) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public Organization getOrganization() {
        return organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * 检查当前用户角色是否具有管理权限
     * @return 是否有管理权限
     */
    public boolean hasAdminPrivilege() {
        try {
            return OrganizationRole.hasAdminPrivilege(OrganizationRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 检查当前用户角色是否具有编辑权限
     * @return 是否有编辑权限
     */
    public boolean hasEditPrivilege() {
        try {
            return OrganizationRole.hasEditPrivilege(OrganizationRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "UserOrganizationLink{" +
                "id=" + id +
                ", organizationId=" + organizationId +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                '}';
    }
} 