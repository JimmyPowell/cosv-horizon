package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.enums.OrganizationStatus;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrganizationService {
    private final OrganizationMapper organizationMapper;
    private final UserMapper userMapper;
    private final tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper vmMapper;
    private final tech.cspioneer.backend.mapper.OrgInviteLinkMapper inviteLinkMapper;
    private final tech.cspioneer.backend.mapper.NotificationMapper notificationMapper;

    public AdminOrganizationService(OrganizationMapper organizationMapper,
                                    UserMapper userMapper,
                                    tech.cspioneer.backend.mapper.VulnerabilityMetadataMapper vmMapper,
                                    tech.cspioneer.backend.mapper.OrgInviteLinkMapper inviteLinkMapper,
                                    tech.cspioneer.backend.mapper.NotificationMapper notificationMapper) {
        this.organizationMapper = organizationMapper;
        this.userMapper = userMapper;
        this.vmMapper = vmMapper;
        this.inviteLinkMapper = inviteLinkMapper;
        this.notificationMapper = notificationMapper;
    }

    public record Page<T>(List<T> items, long total) {}

    public Page<Organization> list(String q, String status, int page, int size) {
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        String s = emptyToNull(normalizeStatus(status));
        var items = organizationMapper.listAdmin(emptyToNull(trimToNull(q)), s, limit, offset);
        long total = organizationMapper.countAdmin(emptyToNull(trimToNull(q)), s);
        return new Page<>(items, total);
    }

    public Organization getByUuid(String uuid) {
        Organization o = organizationMapper.findByUuid(uuid);
        if (o == null) throw new ApiException(404, "组织不存在");
        return o;
    }

    public Organization updateStatus(String adminUuid, String orgUuid, String status, String rejectReason) {
        Organization o = organizationMapper.findByUuid(orgUuid);
        if (o == null) throw new ApiException(404, "组织不存在");
        String s = normalizeStatus(status);
        User admin = userMapper.findByUuid(adminUuid);
        Long reviewerId = admin != null ? admin.getId() : null;
        LocalDateTime reviewDate = LocalDateTime.now();
        boolean rejectReasonSet = rejectReason != null; // 允许显式置空
        organizationMapper.updateStatusReviewByUuid(orgUuid, s, rejectReason, rejectReasonSet, reviewDate, reviewerId);
        return organizationMapper.findByUuid(orgUuid);
    }

    public Organization updateStatusAndVerification(String adminUuid, String orgUuid, String status, String rejectReason, Boolean isVerified) {
        Organization updated = null;
        if (status != null && !status.isBlank()) {
            updated = updateStatus(adminUuid, orgUuid, status, rejectReason);
        }
        if (isVerified != null) {
            Organization patch = new Organization();
            patch.setUuid(orgUuid);
            patch.setIsVerified(isVerified);
            organizationMapper.updateBasic(patch);
            updated = organizationMapper.findByUuid(orgUuid);
        }
        if (updated == null) updated = organizationMapper.findByUuid(orgUuid);
        return updated;
    }

    public Organization updateFull(String adminUuid,
                                   String orgUuid,
                                   String status,
                                   String rejectReason,
                                   Boolean isVerified,
                                   String name,
                                   String avatar,
                                   String description,
                                   String freeText,
                                   Boolean isPublic,
                                   Boolean allowJoinRequest,
                                   Boolean allowInviteLink) {
        Organization updated = null;
        // status / review
        if (status != null && !status.isBlank()) {
            updated = updateStatus(adminUuid, orgUuid, status, rejectReason);
        }
        // name uniqueness
        if (name != null && !name.isBlank()) {
            Organization byName = organizationMapper.findByName(name.trim());
            if (byName != null && !byName.getUuid().equals(orgUuid)) {
                throw new ApiException(1006, "组织名称已存在");
            }
        }
        // basic patch
        if (isVerified != null || name != null || avatar != null || description != null || freeText != null || isPublic != null || allowJoinRequest != null || allowInviteLink != null) {
            Organization patch = new Organization();
            patch.setUuid(orgUuid);
            if (isVerified != null) patch.setIsVerified(isVerified);
            patch.setName(name);
            patch.setAvatar(avatar);
            patch.setDescription(description);
            patch.setFreeText(freeText);
            patch.setIsPublic(isPublic);
            patch.setAllowJoinRequest(allowJoinRequest);
            patch.setAllowInviteLink(allowInviteLink);
            organizationMapper.updateBasic(patch);
            updated = organizationMapper.findByUuid(orgUuid);
        }
        if (updated == null) updated = organizationMapper.findByUuid(orgUuid);
        return updated;
    }

    // ===== Admin actions (no org membership required) =====
    public void disbandByAdmin(String adminUuid, String orgUuid) {
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        if (org.getStatus() != OrganizationStatus.ACTIVE) {
            throw new ApiException(1006, "组织当前状态不允许解散");
        }
        organizationMapper.updateStatusAndPoliciesByUuid(orgUuid, OrganizationStatus.SUSPENDED.name(), false, false);
        inviteLinkMapper.deactivateAllByOrgId(org.getId());
        notificationMapper.expireActiveInvitesByOrg(org.getId());
        notificationMapper.expireActiveJoinRequestsByOrg(org.getId());
    }

    public void suspendByAdmin(String adminUuid, String orgUuid) {
        disbandByAdmin(adminUuid, orgUuid);
    }

    public void restoreByAdmin(String adminUuid, String orgUuid) {
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        if (org.getStatus() != OrganizationStatus.SUSPENDED) {
            throw new ApiException(1006, "仅暂停状态可恢复");
        }
        organizationMapper.updateStatusAndPoliciesByUuid(orgUuid, OrganizationStatus.ACTIVE.name(), null, null);
    }

    public void deleteByAdmin(String adminUuid, String orgUuid) {
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        if (org.getStatus() != OrganizationStatus.SUSPENDED) {
            throw new ApiException(1006, "删除前请先暂停组织");
        }
        String shortCode = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toLowerCase();
        String newName = org.getName() + "--deleted-" + shortCode;
        int n = organizationMapper.updateNameAndStatusByUuid(orgUuid, newName, OrganizationStatus.DELETED.name());
        if (n != 1) throw new ApiException(1500, "删除组织失败");
        inviteLinkMapper.deactivateAllByOrgId(org.getId());
        notificationMapper.expireActiveInvitesByOrg(org.getId());
        notificationMapper.expireActiveJoinRequestsByOrg(org.getId());
    }

    public java.util.Map<String, Object> getDetails(String orgUuid, int page, int size) {
        Organization o = organizationMapper.findByUuid(orgUuid);
        if (o == null) throw new ApiException(404, "组织不存在");
        int limit = Math.max(1, Math.min(100, size <= 0 ? 10 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);

        long vulnTotal = vmMapper.countByFiltersAdv(null, null, null, null, null, null, o.getId(), null, null, null, null, null, false, false, false, false, null, null, null, false, false);
        var vulns = vmMapper.listByFiltersAdv(null, null, null, null, null, null, o.getId(), null, null, null, null, null, false, false, false, false, null, null, "vm.modified DESC", limit, offset, null, false, false);
        var members = organizationMapper.listMembers(o.getId());

        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        java.util.Map<String, Object> orgView = new java.util.LinkedHashMap<>();
        orgView.put("uuid", o.getUuid()); orgView.put("name", o.getName()); orgView.put("status", o.getStatus()); orgView.put("avatar", o.getAvatar()); orgView.put("description", o.getDescription()); orgView.put("freeText", o.getFreeText()); orgView.put("isVerified", o.getIsVerified()); orgView.put("dateCreated", o.getDateCreated());
        data.put("organization", orgView);
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("memberCount", members == null ? 0 : members.size());
        stats.put("vulnCount", vulnTotal);
        data.put("statistics", stats);
        data.put("members", members);
        data.put("vulnerabilities", vulns);
        data.put("page", page); data.put("size", limit); data.put("totalVulnerabilities", vulnTotal);
        return data;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try { return OrganizationStatus.fromCode(status.trim()).getCode(); } catch (Exception e) {
            throw new ApiException(1001, "参数错误: 不支持的status");
        }
    }

    private String trimToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
