package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
// 注意：避免与 Swagger 的 @Tag 注解命名冲突，这里不导入实体 Tag，直接使用全限定名。
import tech.cspioneer.backend.mapper.TagMapper;
import tech.cspioneer.backend.mapper.LnkVulnTagMapper;
import tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tags")
@Tag(name = "标签管理")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    private final TagMapper tagMapper;
    private final LnkVulnTagMapper linkTagMapper;
    private final VulnerabilityMetadataMapper vmMapper;

    public TagController(TagMapper tagMapper, LnkVulnTagMapper linkTagMapper, VulnerabilityMetadataMapper vmMapper) {
        this.tagMapper = tagMapper;
        this.linkTagMapper = linkTagMapper;
        this.vmMapper = vmMapper;
    }

    @GetMapping
    @Operation(summary = "标签列表")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:read')")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                 @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal) {
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        java.util.List<tech.cspioneer.backend.entity.Tag> items = tagMapper.list((q == null || q.isBlank()) ? null : q, limit, offset);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", items);
        if (withTotal) {
            long total = tagMapper.count((q == null || q.isBlank()) ? null : q);
            data.put("total", total);
        }
        return ApiResponse.success(data);
    }

    public static class CreateReq { @NotBlank public String code; @NotBlank public String name; }

    @PostMapping
    @Operation(summary = "创建标签（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateReq req) {
        String cd = req.code.trim();
        String nm = req.name.trim();
        if (cd.isBlank() || nm.isBlank()) return ApiResponse.error(400, "code/name 不能为空");
        if (tagMapper.countByCode(cd) > 0) return ApiResponse.error(1006, "标签代码已存在");
        if (tagMapper.countByName(nm) > 0) return ApiResponse.error(1006, "标签名称已存在");
        tech.cspioneer.backend.entity.Tag t = new tech.cspioneer.backend.entity.Tag();
        t.setUuid(java.util.UUID.randomUUID().toString());
        t.setCode(cd);
        t.setName(nm);
        tagMapper.insert(t);
        Map<String, Object> data = new HashMap<>();
        data.put("tag", t);
        return ApiResponse.success(data);
    }

    public static class UpdateReq {
        public String code;
        public String name;
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "更新标签（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                             @Valid @RequestBody UpdateReq req) {
        tech.cspioneer.backend.entity.Tag t = tagMapper.findByUuid(uuid);
        if (t == null) return ApiResponse.error(404, "标签不存在");

        String cd = (req.code == null ? null : req.code.trim());
        String nm = (req.name == null ? null : req.name.trim());

        // 唯一性校验：若有变更
        if (cd != null && !cd.isBlank()) {
            tech.cspioneer.backend.entity.Tag exist = tagMapper.findByCode(cd);
            if (exist != null && !exist.getId().equals(t.getId())) {
                return ApiResponse.error(1006, "标签代码已存在");
            }
        }
        if (nm != null && !nm.isBlank()) {
            tech.cspioneer.backend.entity.Tag exist = tagMapper.findByName(nm);
            if (exist != null && !exist.getId().equals(t.getId())) {
                return ApiResponse.error(1006, "标签名称已存在");
            }
        }

        // 仅在非空白时覆盖
        if (cd != null && !cd.isBlank()) t.setCode(cd);
        if (nm != null && !nm.isBlank()) t.setName(nm);
        tagMapper.updateByUuid(t);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("tag", tagMapper.findByUuid(uuid));
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/usage")
    @Operation(summary = "标签引用情况")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:read')")
    public ApiResponse<Map<String, Object>> usage(@PathVariable("uuid") String uuid) {
        tech.cspioneer.backend.entity.Tag t = tagMapper.findByUuid(uuid);
        if (t == null) return ApiResponse.error(404, "标签不存在");
        long cnt = 0L;
        try { cnt = linkTagMapper.countByTagId(t.getId()); } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("countVulnerabilities", cnt);
        try {
            data.put("samples", vmMapper.sampleByTagId(t.getId(), 5));
        } catch (Exception ignore) {
            data.put("samples", java.util.List.of());
        }
        return ApiResponse.success(data);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "删除标签（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> delete(@PathVariable("uuid") String uuid) {
        tech.cspioneer.backend.entity.Tag t = tagMapper.findByUuid(uuid);
        if (t == null) return ApiResponse.error(404, "标签不存在");
        long cnt = 0L;
        try { cnt = linkTagMapper.countByTagId(t.getId()); } catch (Exception ignore) {}
        if (cnt > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("countVulnerabilities", cnt);
            try {
                data.put("samples", vmMapper.sampleByTagId(t.getId(), 5));
            } catch (Exception ignore) {
                data.put("samples", java.util.List.of());
            }
            return new ApiResponse<>(1013, "该标签已被使用，可选择强制删除或迁移", data);
        }
        tagMapper.deleteById(t.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ApiResponse.success(data);
    }

    public static class ForceDeleteReq { public Boolean dryRun; }

    @PostMapping("/{uuid}/actions/force-delete")
    @Operation(summary = "强制删除标签（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ApiResponse<Map<String, Object>> forceDelete(@PathVariable("uuid") String uuid,
                                                        @RequestBody(required = false) ForceDeleteReq req) {
        boolean dry = req != null && Boolean.TRUE.equals(req.dryRun);
        tech.cspioneer.backend.entity.Tag t = tagMapper.findByUuid(uuid);
        if (t == null) return ApiResponse.error(404, "标签不存在");
        long cnt = 0L;
        try { cnt = linkTagMapper.countByTagId(t.getId()); } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("willUnlink", cnt);
        data.put("willDelete", 1);
        if (dry) return ApiResponse.success(data);
        if (cnt > 0) {
            try { linkTagMapper.deleteByTagId(t.getId()); } catch (Exception ignore) {}
        }
        tagMapper.deleteById(t.getId());
        return ApiResponse.success(data);
    }

    public static class RemapReq { public String targetUuid; public Boolean dryRun; }

    @PostMapping("/{uuid}/actions/remap")
    @Operation(summary = "迁移标签引用到新标签（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ApiResponse<Map<String, Object>> remap(@PathVariable("uuid") String uuid,
                                                  @Valid @RequestBody RemapReq req) {
        if (req == null || req.targetUuid == null || req.targetUuid.isBlank()) {
            return ApiResponse.error(1001, "targetUuid 不能为空");
        }
        boolean dry = Boolean.TRUE.equals(req.dryRun);
        tech.cspioneer.backend.entity.Tag from = tagMapper.findByUuid(uuid);
        if (from == null) return ApiResponse.error(404, "标签不存在");
        tech.cspioneer.backend.entity.Tag to = tagMapper.findByUuid(req.targetUuid);
        if (to == null) return ApiResponse.error(1014, "目标标签不存在");
        if (from.getId().equals(to.getId())) return ApiResponse.error(1001, "目标标签不能与源标签相同");
        long cnt = 0L;
        try { cnt = linkTagMapper.countByTagId(from.getId()); } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("willUpdate", cnt);
        if (dry) return ApiResponse.success(data);
        if (cnt > 0) {
            try {
                // 先删除重复，避免迁移后产生重复关联
                linkTagMapper.deleteDuplicatesForMigration(from.getId(), to.getId());
                linkTagMapper.remapTag(from.getId(), to.getId());
            } catch (Exception ignore) {}
        }
        return ApiResponse.success(data);
    }
}
