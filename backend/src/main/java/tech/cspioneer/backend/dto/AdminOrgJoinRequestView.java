package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "组织管理员视角的加入申请项")
public class AdminOrgJoinRequestView {
    private String requestUuid;
    private String status;
    private Boolean isRead;
    private LocalDateTime createTime;

    // 申请人
    private String applicantUuid;
    private String applicantName;
    private String applicantEmail;
    private String applicantAvatar;

    // 申请内容
    private String content;

    public String getRequestUuid() { return requestUuid; }
    public void setRequestUuid(String requestUuid) { this.requestUuid = requestUuid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getApplicantUuid() { return applicantUuid; }
    public void setApplicantUuid(String applicantUuid) { this.applicantUuid = applicantUuid; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
    public String getApplicantAvatar() { return applicantAvatar; }
    public void setApplicantAvatar(String applicantAvatar) { this.applicantAvatar = applicantAvatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

