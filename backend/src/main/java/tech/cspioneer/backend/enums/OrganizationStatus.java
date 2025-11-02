package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 组织状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "组织状态")
public enum OrganizationStatus {
    
    @Schema(description = "活跃")
    ACTIVE("ACTIVE", "活跃"),
    
    @Schema(description = "待审核")
    PENDING("PENDING", "待审核"),
    
    @Schema(description = "已拒绝")
    REJECTED("REJECTED", "已拒绝"),
    
    @Schema(description = "已暂停")
    SUSPENDED("SUSPENDED", "已暂停"),

    @Schema(description = "已封禁（管理员）")
    BANNED("BANNED", "已封禁"),

    @Schema(description = "已解散（由拥有者发起）")
    DISBANDED("DISBANDED", "已解散"),

    @Schema(description = "已删除（软删除）")
    DELETED("DELETED", "已删除");
    
    @Schema(description = "状态代码")
    private final String code;
    
    @Schema(description = "状态描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static OrganizationStatus fromCode(String code) {
        for (OrganizationStatus status : OrganizationStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown organization status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
