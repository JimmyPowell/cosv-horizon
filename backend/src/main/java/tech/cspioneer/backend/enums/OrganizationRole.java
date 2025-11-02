package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 组织角色枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "组织角色")
public enum OrganizationRole {
    
    @Schema(description = "管理员")
    ADMIN("ADMIN", "管理员"),
    
    @Schema(description = "成员")
    MEMBER("MEMBER", "成员"),
    
    @Schema(description = "查看者")
    VIEWER("VIEWER", "查看者");
    
    @Schema(description = "角色代码")
    private final String code;
    
    @Schema(description = "角色描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static OrganizationRole fromCode(String code) {
        for (OrganizationRole role : OrganizationRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown organization role code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
