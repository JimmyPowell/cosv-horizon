package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 项目类型枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "项目类型")
public enum ProjectType {
    
    @Schema(description = "受影响")
    AFFECTED("AFFECTED", "受影响"),
    
    @Schema(description = "已修复")
    FIXED("FIXED", "已修复"),
    
    @Schema(description = "已打补丁")
    PATCHED("PATCHED", "已打补丁");
    
    @Schema(description = "类型代码")
    private final String code;
    
    @Schema(description = "类型描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static ProjectType fromCode(String code) {
        for (ProjectType type : ProjectType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown project type code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
