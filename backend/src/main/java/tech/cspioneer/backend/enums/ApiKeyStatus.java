package tech.cspioneer.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * API密钥状态枚举
 */
@Getter
@AllArgsConstructor
@Schema(description = "API密钥状态")
public enum ApiKeyStatus {
    
    @Schema(description = "活跃")
    ACTIVE("ACTIVE", "活跃"),
    
    @Schema(description = "已撤销")
    REVOKED("REVOKED", "已撤销"),
    
    @Schema(description = "已过期")
    EXPIRED("EXPIRED", "已过期");
    
    @Schema(description = "状态代码")
    private final String code;
    
    @Schema(description = "状态描述")
    private final String description;
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    @JsonCreator
    public static ApiKeyStatus fromCode(String code) {
        for (ApiKeyStatus status : ApiKeyStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown API key status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
