package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tech.cspioneer.backend.entity.Organization;

@Schema(description = "组织与成员角色")
public class OrgWithRole {
    @Schema(description = "组织信息")
    private Organization organization;
    @Schema(description = "当前用户在组织内的角色")
    private String role;

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

