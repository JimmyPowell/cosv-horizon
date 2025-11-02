package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.service.OrganizationService;
import tech.cspioneer.backend.service.PointsPolicyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orgs")
@Tag(name = "组织管理")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrganizationController.class);
    private final OrganizationService organizationService;
    private final PointsPolicyService pointsPolicyService;

    public OrganizationController(OrganizationService organizationService, PointsPolicyService pointsPolicyService) {
        this.organizationService = organizationService;
        this.pointsPolicyService = pointsPolicyService;
    }

    public static class CreateOrgReq {
        @NotBlank public String name;
        public String avatar;
        public String description;
        public String freeText;
        public Boolean isPublic;
        public Boolean allowJoinRequest;
        public Boolean allowInviteLink;
    }

    @PostMapping
    @Operation(summary = "创建组织")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateOrgReq req, Principal principal) {
        String userUuid = principal.getName();
        Organization org = organizationService.createOrganization(userUuid, req.name, req.avatar, req.description,
                req.freeText, req.isPublic, req.allowJoinRequest, req.allowInviteLink);
        Map<String, Object> data = new HashMap<>();
        data.put("organization", orgView(org));
        return ApiResponse.success(data);
    }

    @GetMapping("/me")
    @Operation(summary = "我的组织列表（包含角色）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> myOrgs(Principal principal) {
        String userUuid = principal.getName();
        List<OrgWithRole> list = organizationService.listMyOrgs(userUuid);
        Map<String, Object> data = new HashMap<>();
        data.put("items", list.stream().map(this::orgWithRoleView).toList());
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "组织详情")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> get(@PathVariable("uuid") String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new ApiException(1001, "参数错误: 组织UUID不能为空");
        }
        // 可见性控制：公开组织任何已登录用户可见；私有组织仅成员可见
        String userUuid = java.util.Optional.ofNullable(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()).map(a -> a.getName()).orElse(null);
        Organization org = organizationService.getForView(userUuid, uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("organization", orgView(org));
        return ApiResponse.success(data);
    }

    public static class UpdateOrgReq {
        public String name;
        public String avatar;
        public String description;
        public String freeText;
        public Boolean isPublic;
        public Boolean allowJoinRequest;
        public Boolean allowInviteLink;
    }

    @PatchMapping("/{uuid}")
    @Operation(summary = "更新组织基础信息（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Map<String, Object>> update(@PathVariable("uuid") String uuid,
                                                   @Valid @RequestBody UpdateOrgReq req,
                                                   Principal principal) {
        String userUuid = principal.getName();
        Organization org = organizationService.updateBasic(userUuid, uuid, req.name, req.avatar, req.description,
                req.freeText, req.isPublic, req.allowJoinRequest, req.allowInviteLink);
        Map<String, Object> data = new HashMap<>();
        data.put("organization", orgView(org));
        return ApiResponse.success(data);
    }

    /*
    // TEMPORARILY DISABLED: Direct add member without confirmation.
    // Keeping code for future re-enable; prefer invite/accept flow for now.
    public static class MemberReq {
        @NotBlank public String loginOrEmail;
        @NotBlank public String role;
    }

    @PostMapping("/{uuid}/members/add")
    @Operation(summary = "添加成员（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> addMember(@PathVariable("uuid") String uuid,
                                       @Valid @RequestBody MemberReq req,
                                       Principal principal) {
        String userUuid = principal.getName();
        organizationService.addMember(userUuid, uuid, req.loginOrEmail, req.role);
        return ApiResponse.success(null);
    }
    */

    public static class ChangeRoleReq {
        @NotBlank public String memberUuid;
        @NotBlank public String role;
    }

    @PostMapping("/{uuid}/members/change-role")
    @Operation(summary = "变更成员角色（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> changeRole(@PathVariable("uuid") String uuid,
                                        @Valid @RequestBody ChangeRoleReq req,
                                        Principal principal) {
        String userUuid = principal.getName();
        organizationService.changeMemberRole(userUuid, uuid, req.memberUuid, req.role);
        return ApiResponse.success(null);
    }

    public static class RemoveMemberReq {
        @NotBlank public String memberUuid;
    }

    @PostMapping("/{uuid}/members/remove")
    @Operation(summary = "移除成员（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> removeMember(@PathVariable("uuid") String uuid,
                                          @Valid @RequestBody RemoveMemberReq req,
                                          Principal principal) {
        String userUuid = principal.getName();
        organizationService.removeMember(userUuid, uuid, req.memberUuid);
        return ApiResponse.success(null);
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
        m.put("isVerified", o.getIsVerified());
        m.put("rating", o.getRating());
        m.put("isPublic", o.getIsPublic());
        m.put("allowJoinRequest", o.getAllowJoinRequest());
        m.put("allowInviteLink", o.getAllowInviteLink());
        return m;
    }

    private Map<String, Object> orgWithRoleView(OrgWithRole ow) {
        Map<String, Object> m = orgView(ow.getOrganization());
        m.put("role", ow.getRole());
        return m;
    }

    // ===== Invite Links =====
    public static class CreateInviteLinkReq { public Integer expiresInDays; }

    @PostMapping("/{uuid}/invite-links")
    @Operation(summary = "生成邀请链接（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Map<String, Object>> createInviteLink(@PathVariable("uuid") String uuid,
                                                             @Valid @RequestBody(required = false) CreateInviteLinkReq req,
                                                             Principal principal) {
        var link = organizationService.generateInviteLink(principal.getName(), uuid, req == null ? null : req.expiresInDays);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> v = new HashMap<>();
        v.put("uuid", link.getUuid());
        v.put("code", link.getCode());
        v.put("createTime", link.getCreateTime());
        v.put("expireTime", link.getExpireTime());
        v.put("isActive", link.getIsActive());
        data.put("inviteLink", v);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/invite-links")
    @Operation(summary = "邀请链接列表（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> listInviteLinks(@PathVariable("uuid") String uuid, Principal principal) {
        var list = organizationService.listInviteLinks(principal.getName(), uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("items", list.stream().map(l -> {
            Map<String, Object> v = new HashMap<>();
            v.put("uuid", l.getUuid());
            v.put("code", l.getCode());
            v.put("createTime", l.getCreateTime());
            v.put("expireTime", l.getExpireTime());
            v.put("isActive", l.getIsActive());
            return v;
        }).toList());
        return ApiResponse.success(data);
    }

    @PostMapping("/invite-links/{linkUuid}/revoke")
    @Operation(summary = "撤销邀请链接（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> revokeInviteLink(@PathVariable("linkUuid") String linkUuid, Principal principal) {
        organizationService.revokeInviteLink(principal.getName(), linkUuid);
        return ApiResponse.success(null);
    }

    public static class ApplyByCodeReq { @NotBlank public String code; public String message; }

    @PostMapping("/invite-links/apply")
    @Operation(summary = "使用邀请码申请加入（登录用户）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Void> applyByInviteCode(@Valid @RequestBody ApplyByCodeReq req, Principal principal) {
        organizationService.applyJoinByInviteCode(principal.getName(), req.code, req.message);
        return ApiResponse.success(null);
    }

    @GetMapping("/{uuid}/members")
    @Operation(summary = "组织成员列表（成员可见）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> members(@PathVariable("uuid") String uuid, Principal principal) {
        String userUuid = principal.getName();
        var list = organizationService.listMembers(userUuid, uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("items", list);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/members/public")
    @Operation(summary = "公开组织成员精简列表（登录用户）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> publicMembers(@PathVariable("uuid") String uuid, Principal principal) {
        String userUuid = principal != null ? principal.getName() : null;
        var list = organizationService.listPublicMembers(userUuid, uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("items", list);
        return ApiResponse.success(data);
    }

    @GetMapping("/search")
    @Operation(summary = "公开组织搜索（登录用户）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> search(@RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                   @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                   @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal) {
        var p = organizationService.searchPublic(q, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", p.items.stream().map(this::orgView).toList());
        if (withTotal) data.put("total", p.total);
        return ApiResponse.success(data);
    }

    // ===== Join Requests =====
    public static class JoinRequestBody { public String message; }

    @PostMapping("/{uuid}/join-requests")
    @Operation(summary = "提交加入申请（公开组织且允许申请）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<Void> submitJoin(@PathVariable("uuid") String uuid,
                                        @Valid @RequestBody(required = false) JoinRequestBody body,
                                        Principal principal) {
        organizationService.submitJoinRequest(principal.getName(), uuid, body == null ? null : body.message);
        return ApiResponse.success(null);
    }

    @GetMapping("/{uuid}/join-requests")
    @Operation(summary = "加入申请列表（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> listJoin(@PathVariable("uuid") String uuid,
                                                     @RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                     @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                     Principal principal) {
        var p = organizationService.listJoinRequests(principal.getName(), uuid, status, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", p.items);
        data.put("total", p.total);
        return ApiResponse.success(data);
    }

    @PostMapping("/join-requests/{requestUuid}/approve")
    @Operation(summary = "审批通过加入申请（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> approveJoin(@PathVariable("requestUuid") String requestUuid, Principal principal) {
        organizationService.approveJoinRequest(principal.getName(), requestUuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/join-requests/{requestUuid}/reject")
    @Operation(summary = "拒绝加入申请（管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> rejectJoin(@PathVariable("requestUuid") String requestUuid, Principal principal) {
        organizationService.rejectJoinRequest(principal.getName(), requestUuid);
        return ApiResponse.success(null);
    }

    public static class InviteReq {
        @NotBlank public String loginOrEmail;
    }

    @PostMapping("/{uuid}/invitations/invite")
    @Operation(summary = "发起组织邀请（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> invite(@PathVariable("uuid") String uuid,
                                    @Valid @RequestBody InviteReq req,
                                    Principal principal) {
        String userUuid = principal.getName();
        organizationService.sendInvite(userUuid, uuid, req.loginOrEmail);
        return ApiResponse.success(null);
    }

    @PostMapping("/invitations/{inviteUuid}/accept")
    @Operation(summary = "接受组织邀请（被邀请者）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> accept(@PathVariable("inviteUuid") String inviteUuid, Principal principal) {
        String userUuid = principal.getName();
        organizationService.acceptInvitation(userUuid, inviteUuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/invitations/{inviteUuid}/reject")
    @Operation(summary = "拒绝组织邀请（被邀请者）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> reject(@PathVariable("inviteUuid") String inviteUuid, Principal principal) {
        String userUuid = principal.getName();
        organizationService.rejectInvitation(userUuid, inviteUuid);
        return ApiResponse.success(null);
    }

    @GetMapping("/invitations/mine")
    @Operation(summary = "我的组织邀请列表（被邀请者视角）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Map<String, Object>> myInvitations(@RequestParam(value = "status", required = false) String status,
                                                          @RequestParam(value = "isRead", required = false) Boolean isRead,
                                                          @RequestParam(value = "orgUuid", required = false) String orgUuid,
                                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                          @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal,
                                                          Principal principal) {
        // 状态参数简单校验
        if (status != null && !status.isBlank()) {
            String s = status.trim();
            if (!("ACTIVE".equals(s) || "ACCEPTED".equals(s) || "REJECTED".equals(s) || "EXPIRED".equals(s))) {
                throw new tech.cspioneer.backend.common.ApiException(1001, "参数错误: 不支持的status");
            }
        }
        var pageData = organizationService.listMyInvitations(principal.getName(), status, isRead, orgUuid, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", pageData.items);
        if (withTotal) data.put("total", pageData.total);
        return ApiResponse.success(data);
    }

    @GetMapping("/{uuid}/invitations")
    @Operation(summary = "组织内邀请列表（管理员视角）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> orgInvitations(@PathVariable("uuid") String uuid,
                                                           @RequestParam(value = "status", required = false) String status,
                                                           @RequestParam(value = "inviterUuid", required = false) String inviterUuid,
                                                           @RequestParam(value = "q", required = false) String q,
                                                           @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                           @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                           @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal,
                                                           Principal principal) {
        if (status != null && !status.isBlank()) {
            String s = status.trim();
            if (!("ACTIVE".equals(s) || "ACCEPTED".equals(s) || "REJECTED".equals(s) || "EXPIRED".equals(s))) {
                throw new tech.cspioneer.backend.common.ApiException(1001, "参数错误: 不支持的status");
            }
        }
        String userUuid = principal.getName();
        var pageData = organizationService.listOrgInvitations(userUuid, uuid, status, inviterUuid, q, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", pageData.items);
        if (withTotal) data.put("total", pageData.total);
        return ApiResponse.success(data);
    }

    @PostMapping("/{uuid}/actions/disband")
    @Operation(summary = "解散组织（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> disband(@PathVariable("uuid") String uuid, Principal principal) {
        organizationService.disbandOrganization(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/suspend")
    @Operation(summary = "暂停组织（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> suspend(@PathVariable("uuid") String uuid, Principal principal) {
        organizationService.suspendOrganization(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/restore")
    @Operation(summary = "恢复组织（仅管理员）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> restore(@PathVariable("uuid") String uuid, Principal principal) {
        organizationService.restoreOrganization(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{uuid}/actions/delete")
    @Operation(summary = "删除组织（软删除，仅管理员，需先暂停）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:write')")
    public ApiResponse<Void> delete(@PathVariable("uuid") String uuid, Principal principal) {
        organizationService.deleteOrganization(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    // ===== 组织积分策略（覆盖全局） =====
    @GetMapping("/{uuid}/settings/points")
    @Operation(summary = "组织积分策略（覆盖全局）-读取，管理员")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_org:read')")
    public ApiResponse<Map<String, Object>> getOrgPointsSettings(@PathVariable("uuid") String uuid, Principal principal) {
        organizationService.ensureAdminByUuid(principal.getName(), uuid);
        Map<String, Object> data = new HashMap<>();
        data.put("settings", pointsPolicyService.getSettingsAsMapForOrg(uuid));
        return ApiResponse.success(data);
    }

    public static class OrgPointsSettingsReq { public Map<String, Object> events; public Map<String, Object> severity; }

    @PutMapping("/{uuid}/settings/points")
    @Operation(summary = "组织积分策略（覆盖全局）-更新，管理员")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> updateOrgPointsSettings(@PathVariable("uuid") String uuid,
                                                                    @Valid @RequestBody OrgPointsSettingsReq req,
                                                                    Principal principal) {
        log.info("[Org] Update points settings request orgUuid={} userUuid={}", uuid, principal != null ? principal.getName() : null);
        if (req != null) {
            try { log.debug("[Org] Update payload eventsKeys={} severityMode={}", req.events != null ? req.events.keySet() : null, req.severity != null ? req.severity.get("mode") : null);} catch (Exception ignore) {}
        }
        organizationService.ensureAdminByUuid(principal.getName(), uuid);
        Map<String, Object> patch = new HashMap<>();
        if (req.events != null) patch.put("events", req.events);
        if (req.severity != null) patch.put("severity", req.severity);
        pointsPolicyService.updateSettingsFromMapForOrg(uuid, patch);
        Map<String, Object> data = new HashMap<>();
        data.put("settings", pointsPolicyService.getSettingsAsMapForOrg(uuid));
        return ApiResponse.success(data);
    }

    public static class OrgPointsPreviewReq { public String event; public Double severityNum; public String severityLevel; }

    @PostMapping("/{uuid}/settings/points/preview")
    @Operation(summary = "组织积分策略（覆盖全局）-预览计算，管理员")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> previewOrgPoints(@PathVariable("uuid") String uuid,
                                                             @Valid @RequestBody OrgPointsPreviewReq req,
                                                             Principal principal) {
        log.info("[Org] Preview points request orgUuid={} userUuid={}", uuid, principal != null ? principal.getName() : null);
        organizationService.ensureAdminByUuid(principal.getName(), uuid);
        tech.cspioneer.backend.service.PointsPolicyService.PreviewReq r = new tech.cspioneer.backend.service.PointsPolicyService.PreviewReq();
        r.event = req.event; r.severityNum = req.severityNum; r.severityLevel = req.severityLevel;
        var out = pointsPolicyService.previewForOrg(uuid, r);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("userDelta", out.userDelta);
        result.put("orgDelta", out.orgDelta);
        result.put("details", out.details);
        data.put("result", result);
        return ApiResponse.success(data);
    }
}
