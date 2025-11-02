package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 通知状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "通知状态")
public enum NotificationStatus {
    
    @Schema(description = "活跃")
    ACTIVE("ACTIVE", "活跃"),
    
    @Schema(description = "已过期")
    EXPIRED("EXPIRED", "已过期"),
    
    @Schema(description = "已删除")
    DELETED("DELETED", "已删除"),

    @Schema(description = "已接受（邀请）")
    ACCEPTED("ACCEPTED", "已接受"),

    @Schema(description = "已拒绝（邀请）")
    REJECTED("REJECTED", "已拒绝");
    
    @Schema(description = "状态代码")
    private final String code;
    
    @Schema(description = "状态描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static NotificationStatus fromCode(String code) {
        for (NotificationStatus status : NotificationStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown notification status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
