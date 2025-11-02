package tech.cspioneer.backend.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.service.AdminOrganizationService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/orgs")
@Tag(name = "管理员-组织管理")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminOrganizationController {
    private final AdminOrganizationService service;

    public AdminOrganizationController(AdminOrganizationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "组织列表（管理员）")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                 @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal) {
        var p = service.list(q, status, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", p.items());
        if (withTotal) data.put("total", p.total());
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "组织详情（管理员）")
    public ApiResponse<Map<String, Object>> get(@PathVariable("uuid") String uuid) {
        Organization o = service.getByUuid(uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("organization", orgView(o));
        return ApiResponse.success(data);
    }

    public static class UpdateReq {
        public String status; public String rejectReason; public Boolean isVerified;
        public String name; public String avatar; public String description; public String freeText;
        public Boolean isPublic; public Boolean allowJoinRequest; public Boolean allowInviteLink;
    }

    @PatchMapping("/{uuid}")
    @Operation(summary = "更新组织状态/审核信息（管理员）")
    public ApiResponse<Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                   @Valid @RequestBody UpdateReq req,
                                                   Principal principal) {
        Organization updated = service.updateFull(principal.getName(), uuid,
                req.status, req.rejectReason, req.isVerified,
                req.name, req.avatar, req.description, req.freeText,
                req.isPublic, req.allowJoinRequest, req.allowInviteLink);
        Map<String, Object> data = new HashMap<>();
        data.put("organization", orgView(updated));
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/details")
    @Operation(summary = "组织聚合详情（管理员）")
    public ApiResponse<Map<String, Object>> details(@PathVariable("uuid") String uuid,
                                                    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                    @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        Map<String, Object> data = service.getDetails(uuid, page, size);
        return ApiResponse.success(data);
    }

    private Map<String, Object> orgView(Organization o) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", o.getUuid());
        m.put("name", o.getName());
        m.put("status", o.getStatus());
        m.put("dateCreated", o.getDateCreated());
        m.put("avatar", o.getAvatar());
        m.put("description", o.getDescription());
        m.put("freeText", o.getFreeText());
        m.put("rating", o.getRating());
        m.put("rejectReason", o.getRejectReason());
        m.put("reviewDate", o.getReviewDate());
        m.put("reviewedBy", o.getReviewedBy());
        m.put("isVerified", o.getIsVerified());
        m.put("isPublic", o.getIsPublic());
        m.put("allowJoinRequest", o.getAllowJoinRequest());
        m.put("allowInviteLink", o.getAllowInviteLink());
        return m;
    }

    @PostMapping("/{uuid}/actions/disband")
    @Operation(summary = "解散组织（站点管理员）")
    public ApiResponse<Void> disband(@PathVariable("uuid") String uuid, Principal principal) {
        service.disbandByAdmin(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/suspend")
    @Operation(summary = "暂停组织（站点管理员）")
    public ApiResponse<Void> suspend(@PathVariable("uuid") String uuid, Principal principal) {
        service.suspendByAdmin(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/restore")
    @Operation(summary = "恢复组织（站点管理员）")
    public ApiResponse<Void> restore(@PathVariable("uuid") String uuid, Principal principal) {
        service.restoreByAdmin(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/delete")
    @Operation(summary = "删除组织（软删除，站点管理员，需先暂停）")
    public ApiResponse<Void> delete(@PathVariable("uuid") String uuid, Principal principal) {
        service.deleteByAdmin(principal.getName(), uuid);
        return ApiResponse.success(null);
    }
}
