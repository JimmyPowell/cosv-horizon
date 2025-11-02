package tech.cspioneer.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.search.EsIndexer;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/search")
@Tag(name = "管理员-搜索索引")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminSearchController {
    private final EsIndexer indexer;

    public AdminSearchController(org.springframework.beans.factory.ObjectProvider<EsIndexer> provider) {
        this.indexer = provider.getIfAvailable();
    }

    private void ensureEnabled() {
        if (indexer == null) throw new RuntimeException("ES 未启用或未配置");
    }

    @PostMapping("/reindex-all")
    @Operation(summary = "全量重建索引")
    public ApiResponse<Map<String, Object>> reindexAll() {
        ensureEnabled();
        int count = indexer.reindexAll();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ApiResponse.success(data);
    }

    @PostMapping("/index/{uuid}")
    @Operation(summary = "单条索引重建")
    public ApiResponse<Void> indexOne(@PathVariable("uuid") String uuid) {
        ensureEnabled();
        indexer.indexOne(uuid);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/index/{uuid}")
    @Operation(summary = "从索引中删除文档")
    public ApiResponse<Void> deleteOne(@PathVariable("uuid") String uuid) {
        ensureEnabled();
        indexer.deleteOne(uuid);
        return ApiResponse.success(null);
    }
}

