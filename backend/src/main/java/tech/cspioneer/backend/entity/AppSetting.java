package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用设置键值对")
public class AppSetting {
    @Schema(description = "键名")
    private String key;
    @Schema(description = "值")
    private String value;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

