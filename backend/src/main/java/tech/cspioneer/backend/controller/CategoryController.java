package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.Category;
import tech.cspioneer.backend.mapper.CategoryMapper;
import tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@Tag(name = "分类管理")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    private final CategoryMapper categoryMapper;
    private final VulnerabilityMetadataMapper vmMapper;

    public CategoryController(CategoryMapper categoryMapper, VulnerabilityMetadataMapper vmMapper) {
        this.categoryMapper = categoryMapper;
        this.vmMapper = vmMapper;
    }

    @GetMapping
    @Operation(summary = "分类列表")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:read')")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                 @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal) {
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        List<Category> items = categoryMapper.list((q == null || q.isBlank()) ? null : q, limit, offset);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", items);
        if (withTotal) {
            long total = categoryMapper.count((q == null || q.isBlank()) ? null : q);
            data.put("total", total);
        }
        return ApiResponse.success(data);
    }

    public static class CreateReq {
        @NotBlank public String code;
        @NotBlank public String name;
        public String description;
    }

    @PostMapping
    @Operation(summary = "创建分类（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateReq req) {
        Category exist = categoryMapper.findByCode(req.code);
        if (exist != null) return ApiResponse.error(1006, "分类代码已存在");
        Category c = new Category();
        c.setUuid(java.util.UUID.randomUUID().toString());
        c.setCode(req.code);
        c.setName(req.name);
        c.setDescription(req.description);
        categoryMapper.insert(c);
        Map<String, Object> data = new HashMap<>();
        data.put("category", c);
        return ApiResponse.success(data);
    }

    public static class UpdateReq {
        public String code;
        public String name;
        public String description;
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "更新分类（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                   @Valid @RequestBody UpdateReq req) {
        Category c = categoryMapper.findByUuid(uuid);
        if (c == null) return ApiResponse.error(404, "分类不存在");
        if (req.code != null && !req.code.isBlank()) c.setCode(req.code);
        if (req.name != null && !req.name.isBlank()) c.setName(req.name);
        if (req.description != null) c.setDescription(req.description);
        categoryMapper.updateByUuid(c);
        Map<String, Object> data = new HashMap<>();
        data.put("category", categoryMapper.findByUuid(uuid));
        return ApiResponse.success(data);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "删除分类（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> delete(@PathVariable("uuid") String uuid) {
        Category c = categoryMapper.findByUuid(uuid);
        if (c == null) return ApiResponse.error(404, "分类不存在");
        long count = 0L;
        try {
            count = vmMapper.countByCategoryId(c.getId());
        } catch (Exception ignore) {
            // 若相关表未初始化，视为无引用，允许删除
        }
        if (count > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("countVulnerabilities", count);
            try {
                data.put("samples", vmMapper.sampleByCategoryId(c.getId(), 5));
            } catch (Exception ignore) {
                data.put("samples", java.util.List.of());
            }
            return new ApiResponse<>(1013, "该分类已被漏洞引用，可选择强制删除或迁移", data);
        }
        categoryMapper.deleteByUuid(uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/usage")
    @Operation(summary = "分类引用情况")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_vuln:read')")
    public ApiResponse<Map<String, Object>> usage(@PathVariable("uuid") String uuid) {
        Category c = categoryMapper.findByUuid(uuid);
        if (c == null) return ApiResponse.error(404, "分类不存在");
        long count = 0L;
        try {
            count = vmMapper.countByCategoryId(c.getId());
        } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("countVulnerabilities", count);
        try {
            data.put("samples", vmMapper.sampleByCategoryId(c.getId(), 5));
        } catch (Exception ignore) {
            data.put("samples", java.util.List.of());
        }
        return ApiResponse.success(data);
    }

    public static class ForceDeleteReq { public Boolean dryRun; }

    @PostMapping("/{uuid}/actions/force-delete")
    @Operation(summary = "强制删除分类（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ApiResponse<Map<String, Object>> forceDelete(@PathVariable("uuid") String uuid,
                                                        @RequestBody(required = false) ForceDeleteReq req) {
        boolean dry = req != null && Boolean.TRUE.equals(req.dryRun);
        Category c = categoryMapper.findByUuid(uuid);
        if (c == null) return ApiResponse.error(404, "分类不存在");
        long count = 0L;
        try {
            count = vmMapper.countByCategoryId(c.getId());
        } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("willUnlink", count);
        data.put("willDelete", 1);
        if (dry) return ApiResponse.success(data);
        if (count > 0) {
            try { vmMapper.clearCategoryById(c.getId()); } catch (Exception ignore) {}
        }
        categoryMapper.deleteById(c.getId());
        return ApiResponse.success(data);
    }

    public static class RemapReq { public String targetUuid; public Boolean dryRun; }

    @PostMapping("/{uuid}/actions/remap")
    @Operation(summary = "迁移分类引用到新分类（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ApiResponse<Map<String, Object>> remap(@PathVariable("uuid") String uuid,
                                                  @Valid @RequestBody RemapReq req) {
        if (req == null || req.targetUuid == null || req.targetUuid.isBlank()) {
            return ApiResponse.error(1001, "targetUuid 不能为空");
        }
        boolean dry = Boolean.TRUE.equals(req.dryRun);
        Category from = categoryMapper.findByUuid(uuid);
        if (from == null) return ApiResponse.error(404, "分类不存在");
        Category to = categoryMapper.findByUuid(req.targetUuid);
        if (to == null) return ApiResponse.error(1014, "目标分类不存在");
        if (from.getId().equals(to.getId())) return ApiResponse.error(1001, "目标分类不能与源分类相同");
        long count = 0L;
        try {
            count = vmMapper.countByCategoryId(from.getId());
        } catch (Exception ignore) {}
        Map<String, Object> data = new HashMap<>();
        data.put("willUpdate", count);
        if (dry) return ApiResponse.success(data);
        if (count > 0) {
            try { vmMapper.remapCategory(from.getId(), to.getId()); } catch (Exception ignore) {}
        }
        return ApiResponse.success(data);
    }
}
