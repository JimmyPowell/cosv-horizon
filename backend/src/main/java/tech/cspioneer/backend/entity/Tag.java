package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库表：tag
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标签信息")
public class Tag {

    @Schema(description = "标签ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "标签全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440004")
    private String uuid;

    @Schema(description = "标签代码（稳定键）", example = "sql-injection")
    private String code;

    @Schema(description = "标签名称，不可重复", example = "SQL注入")
    private String name;

    @Schema(description = "创建日期", example = "2023-01-01T00:00:00")
    private LocalDateTime createDate;
}
