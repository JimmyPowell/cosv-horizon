package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "用户状态")
public enum UserStatus {
    
    @Schema(description = "已创建")
    CREATED("CREATED", "已创建"),
    
    @Schema(description = "活跃")
    ACTIVE("ACTIVE", "活跃"),
    
    @Schema(description = "非活跃")
    INACTIVE("INACTIVE", "非活跃"),
    
    @Schema(description = "已暂停")
    SUSPENDED("SUSPENDED", "已暂停");
    
    @Schema(description = "状态代码")
    private final String code;
    
    @Schema(description = "状态描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static UserStatus fromCode(String code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
