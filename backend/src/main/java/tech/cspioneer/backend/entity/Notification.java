package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.NotificationType;
import tech.cspioneer.backend.enums.NotificationStatus;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库表：notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知信息")
public class Notification {

    @Schema(description = "通知ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "通知全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440006")
    private String uuid;

    @Schema(description = "通知类型", example = "VULNERABILITY_REPORT")
    private NotificationType type;

    @Schema(description = "目标对象ID", example = "123")
    private Long targetId;

    @Schema(description = "接收者用户ID", example = "1")
    private Long userId;

    @Schema(description = "发送者用户ID", example = "2")
    private Long senderId;

    @Schema(description = "通知标题", example = "新的漏洞报告")
    private String title;

    @Schema(description = "通知内容", example = "您有一个新的漏洞报告需要处理")
    private String content;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    @Schema(description = "创建时间", example = "2023-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "过期时间", example = "2023-02-01T00:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "操作链接", example = "/vulnerabilities/123")
    private String actionUrl;

    @Schema(description = "通知状态", example = "ACTIVE")
    private NotificationStatus status;
}
