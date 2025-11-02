package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "用户角色")
public enum UserRole {
    
    @Schema(description = "管理员")
    ADMIN("ADMIN", "管理员"),
    
    @Schema(description = "普通用户")
    USER("USER", "普通用户"),
    
    @Schema(description = "版主")
    MODERATOR("MODERATOR", "版主");
    
    @Schema(description = "角色代码")
    private final String code;
    
    @Schema(description = "角色描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
