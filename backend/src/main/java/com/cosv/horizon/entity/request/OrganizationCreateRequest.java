package com.cosv.horizon.entity.request;

/**
 * 创建组织请求实体类
 */
public class OrganizationCreateRequest {
    private String name;             // 组织名称
    private String description;      // 组织描述
    private String avatar;           // 组织头像（可选）
    private String freeText;         // 自由描述文本

    // 默认构造函数
    public OrganizationCreateRequest() {
    }

    // 带参数的构造函数
    public OrganizationCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }
} 