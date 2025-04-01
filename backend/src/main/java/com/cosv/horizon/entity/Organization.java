package com.cosv.horizon.entity;

import java.util.Date;

/**
 * 组织实体类
 * 对应数据库中的organization表
 */
public class Organization {
    private Long id;                 // 组织ID，自增主键
    private String name;             // 组织名称，不可为空且唯一
    private String status;           // 组织状态（如ACTIVE, PENDING等）
    private Date dateCreated;        // 组织创建日期
    private String avatar;           // 组织头像路径
    private String description;      // 组织描述
    private Long rating;             // 组织评分
    private String freeText;         // 组织自由描述文本
    private String rejectReason;     // 审核拒绝原因
    private Date reviewDate;         // 审核日期
    private Long reviewedBy;         // 审核者ID

    // 默认构造函数
    public Organization() {
    }

    // 带参数的构造函数
    public Organization(String name, String status) {
        this.name = name;
        this.status = status;
        this.dateCreated = new Date();
        this.rating = 0L;
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
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    public Date getReviewDate() {
        return reviewDate;
    }
    
    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }
    
    public Long getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", dateCreated=" + dateCreated +
                ", rating=" + rating +
                '}';
    }
} 