package com.cosv.horizon.entity.response;

import com.cosv.horizon.entity.Organization;

import java.util.Date;

/**
 * 公开组织信息响应实体类
 * 仅包含对外公开的组织信息
 */
public class PublicOrganizationResponse {
    private Long id;                 // 组织ID
    private String name;             // 组织名称
    private Date dateCreated;        // 创建日期
    private String avatar;           // 头像URL
    private String description;      // 组织描述
    private Long rating;             // 组织评分
    private int memberCount;         // 成员数量

    // 默认构造函数
    public PublicOrganizationResponse() {
    }

    // 从Organization实体构造
    public PublicOrganizationResponse(Organization organization, int memberCount) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.dateCreated = organization.getDateCreated();
        this.avatar = organization.getAvatar();
        this.description = organization.getDescription();
        this.rating = organization.getRating();
        this.memberCount = memberCount;
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
} 