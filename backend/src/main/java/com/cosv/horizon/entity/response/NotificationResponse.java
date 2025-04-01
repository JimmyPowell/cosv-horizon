package com.cosv.horizon.entity.response;

import java.util.Date;

/**
 * 通知响应数据传输对象
 */
public class NotificationResponse {
    private Long id;               // 通知ID
    private String type;           // 通知类型
    private Long targetId;         // 目标对象ID
    private String title;          // 通知标题
    private String content;        // 通知内容
    private boolean isRead;        // 是否已读
    private Date createTime;       // 创建时间
    private String actionUrl;      // 操作链接
    private SenderInfo sender;     // 发送者信息

    /**
     * 发送者信息内部类
     */
    public static class SenderInfo {
        private Long id;           // 发送者ID
        private String username;   // 发送者用户名
        private String avatarUrl;  // 发送者头像

        public SenderInfo() {
        }

        public SenderInfo(Long id, String username, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.avatarUrl = avatarUrl;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
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

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public SenderInfo getSender() {
        return sender;
    }

    public void setSender(SenderInfo sender) {
        this.sender = sender;
    }
} 