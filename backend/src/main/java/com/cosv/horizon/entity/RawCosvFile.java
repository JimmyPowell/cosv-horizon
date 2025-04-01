package com.cosv.horizon.entity;

import java.util.Date;

/**
 * 原始COSV文件实体类
 * 对应数据库中的raw_cosv_file表
 */
public class RawCosvFile {
    private Long id;                 // 自增主键
    private String fileName;         // 文件名称
    private Long userId;             // 上传用户ID
    private Long organizationId;     // 所属组织ID
    private String status;           // 文件状态（如UPLOADED, PROCESSED等）
    private String statusMessage;    // 状态消息（如错误信息）
    private Long contentLength;      // 文件内容长度
    private Date createDate;         // 创建日期
    private Date updateDate;         // 更新日期
    
    // 额外的对象引用，不映射到数据库
    private Organization organization;
    private User user;

    // 默认构造函数
    public RawCosvFile() {
    }

    // 带参数的构造函数
    public RawCosvFile(String fileName, Long userId, Long organizationId, String status) {
        this.fileName = fileName;
        this.userId = userId;
        this.organizationId = organizationId;
        this.status = status;
        this.createDate = new Date();
        this.updateDate = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
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

    @Override
    public String toString() {
        return "RawCosvFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", userId=" + userId +
                ", organizationId=" + organizationId +
                ", status='" + status + '\'' +
                ", contentLength=" + contentLength +
                ", createDate=" + createDate +
                '}';
    }
} 