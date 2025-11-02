package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.ApiKey;
import tech.cspioneer.backend.service.ApiKeyService;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "API Key 管理")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    public static class CreateReq {
        public String organizationUuid; // 为空表示个人 PAT
        public String description;
        public List<String> scopes;
        public String expireTime; // ISO-8601 可选
    }

    @PostMapping
    @Operation(summary = "创建 API Key（返回明文仅一次）")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateReq req, Principal principal) {
        LocalDateTime expire = parseExpireTime(req.expireTime);
        var created = apiKeyService.create(principal.getName(), req.organizationUuid, req.description, req.scopes, expire);
        Map<String, Object> data = new HashMap<>();
        data.put("uuid", created.meta.getUuid());
        data.put("keyPrefix", created.meta.getKeyPrefix());
        data.put("status", created.meta.getStatus());
        data.put("scopes", created.meta.getScopes());
        data.put("expireTime", created.meta.getExpireTime());
        data.put("apiKey", created.fullKey); // 仅此处返回明文
        return ApiResponse.success(data);
    }

    private LocalDateTime parseExpireTime(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        LocalDateTime dt = null;
        try {
            if (s.endsWith("Z")) {
                Instant ins = Instant.parse(s);
                dt = LocalDateTime.ofInstant(ins, ZoneId.systemDefault());
            } else if (s.matches(".*[+-][0-9]{2}:?[0-9]{2}$")) {
                OffsetDateTime odt = OffsetDateTime.parse(s);
                Instant ins = odt.toInstant();
                dt = LocalDateTime.ofInstant(ins, ZoneId.systemDefault());
            }
        } catch (Exception ignore) {}
        if (dt == null) { try { dt = LocalDateTime.parse(s); } catch (Exception ignore) {} }
        if (dt == null) { try { dt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); } catch (Exception ignore) {} }
        if (dt == null) throw new tech.cspioneer.backend.common.ApiException(1001, "expireTime格式无效，支持ISO-8601（可带时区/UTC）或yyyy-MM-dd HH:mm:ss");
        if (!dt.isAfter(LocalDateTime.now())) throw new tech.cspioneer.backend.common.ApiException(1001, "expireTime 已过期或无效，应为未来时间");
        return dt;
    }

    @GetMapping
    @Operation(summary = "列出我的 API Key（或组织 Key）")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "organizationUuid", required = false) String organizationUuid,
                                                 Principal principal) {
        List<ApiKey> items = (organizationUuid == null || organizationUuid.isBlank())
                ? apiKeyService.listMine(principal.getName())
                : apiKeyService.listByOrg(principal.getName(), organizationUuid);
        Map<String, Object> data = new HashMap<>();
        data.put("items", items.stream().map(this::view).toList());
        return ApiResponse.success(data);
    }

    public static class UpdateReq {
        public String description;       // 可选：更新描述
        public List<String> scopes;      // 可选：更新权限列表；空数组表示清空
        public String expireTime;        // 可选：ISO-8601；空字符串表示清除
    }

    @PatchMapping("/{uuid}")
    @Operation(summary = "更新 API Key（描述/权限/过期时间）")
    public ApiResponse<Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                   @RequestBody UpdateReq req,
                                                   Principal principal) {
        ApiKey updated = apiKeyService.update(principal.getName(), uuid, req.description, req.scopes, req.expireTime);
        Map<String, Object> data = new HashMap<>();
        data.put("apiKey", view(updated));
        return ApiResponse.success(data);
    }

    @PostMapping("/{uuid}/revoke")
    @Operation(summary = "吊销 API Key")
    public ApiResponse<Void> revoke(@PathVariable("uuid") String uuid, Principal principal) {
        apiKeyService.revoke(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @GetMapping("/{uuid}/usage")
    @Operation(summary = "查询 API Key 使用日志")
    public ApiResponse<Map<String, Object>> usage(@PathVariable("uuid") String uuid,
                                                  @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                  @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                  @RequestParam(value = "from", required = false) String fromTs,
                                                  @RequestParam(value = "to", required = false) String toTs,
                                                  Principal principal) {
        var pageData = apiKeyService.usage(principal.getName(), uuid, page, size, fromTs, toTs);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("total", pageData.total);
        data.put("items", pageData.items);
        return ApiResponse.success(data);
    }

    private Map<String, Object> view(ApiKey k) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", k.getUuid());
        m.put("keyPrefix", k.getKeyPrefix());
        m.put("description", k.getDescription());
        m.put("scopes", k.getScopes());
        m.put("status", k.getStatus());
        m.put("expireTime", k.getExpireTime());
        m.put("lastUsedTime", k.getLastUsedTime());
        m.put("lastUsedIp", k.getLastUsedIp());
        // 前端需要显示创建日期
        m.put("createTime", k.getCreateTime());
        // 可选：提供更新时间以便审计（前端目前未使用）
        m.put("updateTime", k.getUpdateTime());
        return m;
    }
}
