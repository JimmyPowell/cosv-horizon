package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "我的组织邀请视图")
public class OrgInviteView {
    private String inviteUuid;
    private String status;
    private Boolean isRead;
    private LocalDateTime createTime;

    private String orgUuid;
    private String orgName;
    private String orgAvatar;

    private String inviterUuid;
    private String inviterName;
    private String inviterAvatar;

    public String getInviteUuid() { return inviteUuid; }
    public void setInviteUuid(String inviteUuid) { this.inviteUuid = inviteUuid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getOrgUuid() { return orgUuid; }
    public void setOrgUuid(String orgUuid) { this.orgUuid = orgUuid; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgAvatar() { return orgAvatar; }
    public void setOrgAvatar(String orgAvatar) { this.orgAvatar = orgAvatar; }
    public String getInviterUuid() { return inviterUuid; }
    public void setInviterUuid(String inviterUuid) { this.inviterUuid = inviterUuid; }
    public String getInviterName() { return inviterName; }
    public void setInviterName(String inviterName) { this.inviterName = inviterName; }
    public String getInviterAvatar() { return inviterAvatar; }
    public void setInviterAvatar(String inviterAvatar) { this.inviterAvatar = inviterAvatar; }
}

