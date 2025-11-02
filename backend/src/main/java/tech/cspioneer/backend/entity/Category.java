package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类信息")
public class Category {
    @Schema(description = "分类ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "分类UUID", example = "550e8400-e29b-41d4-a716-446655440010")
    private String uuid;

    @Schema(description = "分类代码，稳定键", example = "CWE")
    private String code;

    @Schema(description = "分类名称", example = "通用弱点枚举")
    private String name;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createDate;
}

