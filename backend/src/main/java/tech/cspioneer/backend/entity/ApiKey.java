package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.ApiKeyStatus;

import java.time.LocalDateTime;

/**
 * API密钥实体类
 * 对应数据库表：api_key
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API密钥信息")
public class ApiKey {

    @Schema(description = "API密钥ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "API密钥的唯一公共标识符", example = "550e8400-e29b-41d4-a716-446655440005")
    private String uuid;

    @Schema(description = "密钥前缀，用于识别", example = "cosv_")
    private String keyPrefix;

    @Schema(description = "密钥的SHA-256哈希值", example = "a1b2c3d4e5f6...")
    private String keyHash;

    @Schema(description = "创建密钥的用户ID", example = "1")
    private Long creatorUserId;

    @Schema(description = "关联的组织ID，NULL表示个人密钥", example = "1")
    private Long organizationId;

    @Schema(description = "用户提供的密钥描述", example = "用于自动化测试的API密钥")
    private String description;

    @Schema(description = "授权范围列表，逗号分隔", example = "read,write,admin")
    private String scopes;

    @Schema(description = "密钥状态", example = "ACTIVE")
    private ApiKeyStatus status;

    @Schema(description = "最后成功使用时间", example = "2023-01-01T12:00:00")
    private LocalDateTime lastUsedTime;

    @Schema(description = "最后成功使用的IP地址", example = "192.168.1.100")
    private String lastUsedIp;

    @Schema(description = "密钥过期时间，NULL表示永不过期", example = "2024-01-01T00:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", example = "2023-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间", example = "2023-01-01T00:00:00")
    private LocalDateTime updateTime;
}
