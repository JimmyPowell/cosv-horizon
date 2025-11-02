package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公开组织成员精简视图")
public class PublicOrgMemberView {
    private String uuid;
    private String name;
    private String avatar;
    private String role;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

