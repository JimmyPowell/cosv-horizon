package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 文件状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "文件状态")
public enum FileStatus {
    
    @Schema(description = "已上传")
    UPLOADED("UPLOADED", "已上传"),
    
    @Schema(description = "处理中")
    PROCESSING("PROCESSING", "处理中"),
    
    @Schema(description = "已处理")
    PROCESSED("PROCESSED", "已处理"),
    
    @Schema(description = "处理失败")
    FAILED("FAILED", "处理失败");
    
    @Schema(description = "状态代码")
    private final String code;
    
    @Schema(description = "状态描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static FileStatus fromCode(String code) {
        for (FileStatus status : FileStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown file status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
