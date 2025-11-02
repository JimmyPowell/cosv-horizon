package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处理后的COSV文件实体类
 * 对应数据库表：cosv_file
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "处理后的COSV文件信息")
public class CosvFile {

    @Schema(description = "文件ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "文件全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440008")
    private String uuid;

    @Schema(description = "COSV文件标识符", example = "COSV-FILE-2023-001")
    private String identifier;

    @Schema(description = "修改时间", example = "2023-01-01T00:00:00")
    private LocalDateTime modified;

    @Schema(description = "前一个版本的COSV文件ID", example = "1")
    private Long prevCosvFileId;

    @Schema(description = "创建/更新文件的用户ID", example = "1")
    private Long userId;

    @Schema(description = "Schema版本", example = "1.0.0")
    private String schemaVersion;

    @Schema(description = "原始COSV文件ID", example = "1")
    private Long rawCosvFileId;
}
