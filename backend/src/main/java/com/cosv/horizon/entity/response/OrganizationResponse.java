package com.cosv.horizon.entity.response;

import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.User;

import java.util.Date;
import java.util.List;

/**
 * 组织信息响应实体类
 */
public class OrganizationResponse {
    private Long id;                 // 组织ID
    private String name;             // 组织名称
    private String status;           // 组织状态
    private Date dateCreated;        // 创建日期
    private String avatar;           // 头像URL
    private String description;      // 组织描述
    private Long rating;             // 组织评分
    private String freeText;         // 自由描述文本
    private List<UserRoleInfo> members; // 组织成员列表
    private int memberCount;         // 成员数量
    private String userRole;         // 当前请求用户在组织中的角色

    // 构造函数
    public OrganizationResponse() {
    }

    // 从Organization实体构造
    public OrganizationResponse(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.status = organization.getStatus();
        this.dateCreated = organization.getDateCreated();
        this.avatar = organization.getAvatar();
        this.description = organization.getDescription();
        this.rating = organization.getRating();
        this.freeText = organization.getFreeText();
    }

    // 内部类：用户角色信息
    public static class UserRoleInfo {
        private Long userId;         // 用户ID
        private String userName;     // 用户名
        private String avatar;       // 用户头像
        private String role;         // 角色

        public UserRoleInfo() {
        }

        public UserRoleInfo(User user, String role) {
            this.userId = user.getId();
            this.userName = user.getName();
            this.avatar = user.getAvatar();
            this.role = role;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public List<UserRoleInfo> getMembers() {
        return members;
    }

    public void setMembers(List<UserRoleInfo> members) {
        this.members = members;
        if (members != null) {
            this.memberCount = members.size();
        }
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
} 