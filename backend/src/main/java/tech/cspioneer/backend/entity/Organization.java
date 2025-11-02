package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.OrganizationStatus;

import java.time.LocalDateTime;

/**
 * 组织实体类
 * 对应数据库表：organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织信息")
public class Organization {

    @Schema(description = "组织ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "组织全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440002")
    private String uuid;

    @Schema(description = "组织名称，不可为空且唯一", example = "COSV Horizon")
    private String name;

    @Schema(description = "组织状态", example = "ACTIVE")
    private OrganizationStatus status;

    @Schema(description = "组织创建日期", example = "2023-01-01T00:00:00")
    private LocalDateTime dateCreated;

    @Schema(description = "组织头像路径", example = "/avatars/org-logo.jpg")
    private String avatar;

    @Schema(description = "组织描述", example = "COSV Horizon官方组织")
    private String description;

    @Schema(description = "组织评分", example = "100")
    private Long rating;

    @Schema(description = "组织自由描述文本", example = "专注于开源漏洞管理")
    private String freeText;

    @Schema(description = "是否已认证")
    private Boolean isVerified;

    @Schema(description = "审核拒绝原因", example = "资料不完整")
    private String rejectReason;

    @Schema(description = "审核日期", example = "2023-01-02T00:00:00")
    private LocalDateTime reviewDate;

    @Schema(description = "审核者ID", example = "1")
    private Long reviewedBy;

    // Visibility and policies
    @Schema(description = "是否公开可见")
    private Boolean isPublic;

    @Schema(description = "是否允许申请加入")
    private Boolean allowJoinRequest;

    @Schema(description = "是否允许生成邀请链接")
    private Boolean allowInviteLink;
}
