package com.cosv.horizon.entity;

import java.util.Date;

/**
 * 通知实体类
 * 用于系统内部各类通知
 */
public class Notification {
    private Long id;               // 通知ID
    private String type;           // 通知类型
    private Long targetId;         // 目标对象ID（如组织ID，转让请求ID等）
    private Long userId;           // 接收者用户ID
    private Long senderId;         // 发送者用户ID（可为空，如系统通知）
    private String title;          // 通知标题
    private String content;        // 通知内容
    private boolean isRead;        // 是否已读
    private Date createTime;       // 创建时间
    private Date expireTime;       // 过期时间（可为空）
    private String actionUrl;      // 操作链接（可为空）
    private String status;         // 通知状态

    // 默认构造函数
    public Notification() {
        this.isRead = false;
        this.createTime = new Date();
        this.status = NotificationStatus.ACTIVE.name();
    }

    // 构造函数
    public Notification(String type, Long targetId, Long userId, String title, String content) {
        this();
        this.type = type;
        this.targetId = targetId;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    // 构造函数（带发送者）
    public Notification(String type, Long targetId, Long userId, Long senderId, String title, String content) {
        this(type, targetId, userId, title, content);
        this.senderId = senderId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", targetId=" + targetId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                '}';
    }
} 