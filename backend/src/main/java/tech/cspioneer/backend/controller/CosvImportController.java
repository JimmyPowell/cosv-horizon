package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.service.CosvImportService;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/cosv")
@Tag(name = "COSV 文件导入")
@SecurityRequirement(name = "bearerAuth")
public class CosvImportController {
    private final CosvImportService importService;

    public CosvImportController(CosvImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/files")
    @Operation(summary = "上传COSV原文（支持以组织身份）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:write')")
    public ApiResponse<Map<String, Object>> upload(Principal principal,
                                                   @RequestPart("file") MultipartFile file,
                                                   @RequestParam(value = "organizationUuid", required = false) String organizationUuid,
                                                   @RequestParam(value = "mimeType", required = false) String mimeType) {
        org.slf4j.LoggerFactory.getLogger(CosvImportController.class).info("[COSV] upload by user={} orgUuid={} fileName={} size={} contentType={}",
                principal != null ? principal.getName() : null,
                organizationUuid,
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : null,
                file != null ? file.getContentType() : null);
        String rawUuid = importService.upload(principal, file, organizationUuid, mimeType);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rawFileUuid", rawUuid);
        return ApiResponse.success(data);
    }

    @PostMapping("/files/{rawFileUuid}/parse")
    @Operation(summary = "解析并预检（不入库）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:write')")
    public ApiResponse<Map<String, Object>> parse(@PathVariable("rawFileUuid") String rawFileUuid,
                                                  @RequestParam(value = "language", required = false) String language,
                                                  @RequestParam(value = "categoryCode", required = false) String categoryCode,
                                                  @RequestParam(value = "tagCodes", required = false) String tagCodes,
                                                  @RequestParam(value = "mode", required = false, defaultValue = "CREATE") String mode) {
        List<String> tags = tagCodes == null || tagCodes.isBlank() ? Collections.emptyList() : Arrays.stream(tagCodes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Map<String, Object> res = importService.parse(rawFileUuid, language, categoryCode, tags, mode);
        return ApiResponse.success(res);
    }

    @PostMapping("/files/{rawFileUuid}/parse-batch")
    @Operation(summary = "解析并预检（批量，不入库）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:write')")
    public ApiResponse<Map<String, Object>> parseBatch(@PathVariable("rawFileUuid") String rawFileUuid,
                                                       @RequestParam(value = "language", required = false) String language,
                                                       @RequestParam(value = "categoryCode", required = false) String categoryCode,
                                                       @RequestParam(value = "tagCodes", required = false) String tagCodes,
                                                       @RequestParam(value = "mode", required = false, defaultValue = "AUTO") String mode) {
        List<String> tags = tagCodes == null || tagCodes.isBlank() ? Collections.emptyList() : Arrays.stream(tagCodes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Map<String, Object> res = importService.parseBatch(rawFileUuid, language, categoryCode, tags, mode);
        return ApiResponse.success(res);
    }

    public static class IngestReq {
        public String action; // CREATE|UPDATE
        public String targetVulnUuid;
        public String conflictPolicy; // FAIL|SKIP_ALIAS|OVERWRITE
        public String publishPolicy; // AUTO|PENDING (预留)
        public String organizationUuid;
        public String language;
        public String categoryCode;
        public String tagCodes; // comma separated
    }

    @PostMapping("/files/{rawFileUuid}/ingest")
    @Operation(summary = "确认入库（创建或更新）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:write')")
    public ApiResponse<Map<String, Object>> ingest(Principal principal,
                                                   @PathVariable("rawFileUuid") @NotBlank String rawFileUuid,
                                                   @RequestBody IngestReq req) {
        List<String> tags = req.tagCodes == null || req.tagCodes.isBlank() ? Collections.emptyList() : Arrays.stream(req.tagCodes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Map<String, Object> res = importService.ingest(principal, rawFileUuid, Optional.ofNullable(req.action).orElse("CREATE"), req.targetVulnUuid,
                Optional.ofNullable(req.conflictPolicy).orElse("FAIL"), Optional.ofNullable(req.publishPolicy).orElse("AUTO"), req.organizationUuid, req.language, req.categoryCode, tags);
        return ApiResponse.success(res);
    }

    public static class IngestBatchReq {
        public String action; // AUTO|CREATE|UPDATE
        public String conflictPolicy; // FAIL|SKIP_ALIAS|OVERWRITE
        public String publishPolicy; // AUTO|PENDING
        public String organizationUuid;
        public String language;
        public String categoryCode;
        public String tagCodes; // comma separated
    }

    @PostMapping("/files/{rawFileUuid}/ingest-batch")
    @Operation(summary = "批量确认入库（创建或更新）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:write')")
    public ApiResponse<Map<String, Object>> ingestBatch(Principal principal,
                                                        @PathVariable("rawFileUuid") @NotBlank String rawFileUuid,
                                                        @RequestBody IngestBatchReq req) {
        List<String> tags = req.tagCodes == null || req.tagCodes.isBlank() ? Collections.emptyList() : Arrays.stream(req.tagCodes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        Map<String, Object> res = importService.ingestBatch(principal, rawFileUuid, Optional.ofNullable(req.action).orElse("AUTO"), Optional.ofNullable(req.conflictPolicy).orElse("FAIL"), Optional.ofNullable(req.publishPolicy).orElse("AUTO"), req.organizationUuid, req.language, req.categoryCode, tags);
        return ApiResponse.success(res);
    }
}
