package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "通知类型")
public enum NotificationType {
    
    @Schema(description = "漏洞报告")
    VULNERABILITY_REPORT("VULNERABILITY_REPORT", "漏洞报告"),
    
    @Schema(description = "系统通知")
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),
    
    @Schema(description = "用户提及")
    USER_MENTION("USER_MENTION", "用户提及"),
    
    @Schema(description = "组织邀请")
    ORGANIZATION_INVITE("ORGANIZATION_INVITE", "组织邀请"),

    @Schema(description = "组织加入申请")
    ORGANIZATION_JOIN_REQUEST("ORGANIZATION_JOIN_REQUEST", "组织加入申请");
    
    @Schema(description = "类型代码")
    private final String code;
    
    @Schema(description = "类型描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static NotificationType fromCode(String code) {
        for (NotificationType type : NotificationType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
