package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.OrgPointsLedger;
import tech.cspioneer.backend.entity.UserPointsLedger;
import tech.cspioneer.backend.service.OrganizationService;
import tech.cspioneer.backend.service.PointsService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@Tag(name = "积分流水")
@SecurityRequirement(name = "bearerAuth")
public class PointsController {
    private final PointsService pointsService;
    private final OrganizationService organizationService;

    public PointsController(PointsService pointsService, OrganizationService organizationService) {
        this.pointsService = pointsService;
        this.organizationService = organizationService;
    }

    @GetMapping("/users/me/points")
    @Operation(summary = "我的积分流水")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> myPoints(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size,
                                                     Principal principal) {
        PointsService.Page<UserPointsLedger> p = pointsService.listUserPoints(principal.getName(), page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("total", p.total);
        data.put("items", p.items);
        return ApiResponse.success(data);
    }

    @GetMapping("/orgs/{uuid}/points")
    @Operation(summary = "组织积分流水（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> orgPoints(@PathVariable("uuid") String uuid,
                                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                                      @RequestParam(value = "size", defaultValue = "20") int size,
                                                      Principal principal) {
        // 复用 OrganizationService 校验范围与权限
        organizationService.getByUuid(uuid); // 范围校验在服务内
        PointsService.Page<OrgPointsLedger> p = pointsService.listOrgPoints(principal.getName(), uuid, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("total", p.total);
        data.put("items", p.items);
        return ApiResponse.success(data);
    }

    @GetMapping("/points/users/{uuid}/summary")
    @Operation(summary = "用户积分汇总（评分与排名）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Map<String, Object>> userSummary(@PathVariable("uuid") String uuid) {
        Map<String, Object> data = new HashMap<>();
        data.put("summary", pointsService.userSummary(uuid));
        return ApiResponse.success(data);
    }

    @GetMapping("/points/orgs/{uuid}/summary")
    @Operation(summary = "组织积分汇总（评分与排名）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> orgSummary(@PathVariable("uuid") String uuid) {
        // 组织范围校验（组织级API Key限制）
        organizationService.getByUuid(uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("summary", pointsService.orgSummary(uuid));
        return ApiResponse.success(data);
    }
}
