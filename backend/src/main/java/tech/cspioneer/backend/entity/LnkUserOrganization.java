package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.OrganizationRole;

/**
 * 用户组织关联实体类
 * 对应数据库表：lnk_user_organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户组织关联关系")
public class LnkUserOrganization {

    @Schema(description = "关联ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "关联关系全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440009")
    private String uuid;

    @Schema(description = "组织ID", example = "1")
    private Long organizationId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户在组织中的角色", example = "ADMIN")
    private OrganizationRole role;
}
