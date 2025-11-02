package tech.cspioneer.backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.cspioneer.backend.enums.FileStatus;

import java.time.LocalDateTime;

/**
 * 原始COSV文件实体类
 * 对应数据库表：raw_cosv_file
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "原始COSV文件信息")
public class RawCosvFile {

    @Schema(description = "文件ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "文件全局唯一标识符", example = "550e8400-e29b-41d4-a716-446655440007")
    private String uuid;

    @Schema(description = "文件名称", example = "vulnerability_report.json")
    private String fileName;

    @Schema(description = "上传用户ID", example = "1")
    private Long userId;

    @Schema(description = "所属组织ID", example = "1")
    private Long organizationId;

    @Schema(description = "文件状态", example = "UPLOADED")
    private FileStatus status;

    @Schema(description = "状态消息", example = "文件处理成功")
    private String statusMessage;

    @Schema(description = "文件内容长度", example = "1024")
    private Long contentLength;

    @Schema(description = "创建日期", example = "2023-01-01T00:00:00")
    private LocalDateTime createDate;

    @Schema(description = "更新日期", example = "2023-01-01T00:00:00")
    private LocalDateTime updateDate;

    @Schema(description = "原文对象存储URL")
    private String storageUrl;

    @Schema(description = "原文内容（二选一，存URL或内容）")
    private byte[] content;

    @Schema(description = "原文SHA-256校验值")
    private String checksumSha256;

    @Schema(description = "原文MIME类型")
    private String mimeType;
}
