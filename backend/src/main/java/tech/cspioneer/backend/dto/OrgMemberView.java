package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "组织成员视图")
public class OrgMemberView {
    private String uuid;
    private String name;
    private String email;
    private String avatar;
    private String role;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

