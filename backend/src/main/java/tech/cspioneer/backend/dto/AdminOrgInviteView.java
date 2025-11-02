package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "组织管理员视角的邀请项")
public class AdminOrgInviteView {
    private String inviteUuid;
    private String status;
    private Boolean isRead;
    private LocalDateTime createTime;

    // 被邀请人
    private String inviteeUuid;
    private String inviteeName;
    private String inviteeEmail;
    private String inviteeAvatar;

    // 发起人
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
    public String getInviteeUuid() { return inviteeUuid; }
    public void setInviteeUuid(String inviteeUuid) { this.inviteeUuid = inviteeUuid; }
    public String getInviteeName() { return inviteeName; }
    public void setInviteeName(String inviteeName) { this.inviteeName = inviteeName; }
    public String getInviteeEmail() { return inviteeEmail; }
    public void setInviteeEmail(String inviteeEmail) { this.inviteeEmail = inviteeEmail; }
    public String getInviteeAvatar() { return inviteeAvatar; }
    public void setInviteeAvatar(String inviteeAvatar) { this.inviteeAvatar = inviteeAvatar; }
    public String getInviterUuid() { return inviterUuid; }
    public void setInviterUuid(String inviterUuid) { this.inviterUuid = inviterUuid; }
    public String getInviterName() { return inviterName; }
    public void setInviterName(String inviterName) { this.inviterName = inviterName; }
    public String getInviterAvatar() { return inviterAvatar; }
    public void setInviterAvatar(String inviterAvatar) { this.inviterAvatar = inviterAvatar; }
}

