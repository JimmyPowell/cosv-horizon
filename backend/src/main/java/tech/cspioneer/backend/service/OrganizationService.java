package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.dto.OrgMemberView;
import tech.cspioneer.backend.entity.LnkUserOrganization;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.LnkUserOrganizationMapper;
import tech.cspioneer.backend.mapper.OrganizationMapper;
import tech.cspioneer.backend.mapper.UserMapper;
import tech.cspioneer.backend.mapper.NotificationMapper;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.enums.OrganizationRole;
import tech.cspioneer.backend.enums.OrganizationStatus;
import tech.cspioneer.backend.enums.NotificationType;
import tech.cspioneer.backend.enums.NotificationStatus;
import tech.cspioneer.backend.dto.OrgInviteView;
import tech.cspioneer.backend.dto.AdminOrgInviteView;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrganizationService.class);
    private final OrganizationMapper organizationMapper;
    private final LnkUserOrganizationMapper lnkMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final tech.cspioneer.backend.mapper.OrgInviteLinkMapper inviteLinkMapper;

    public OrganizationService(OrganizationMapper organizationMapper, LnkUserOrganizationMapper lnkMapper, UserMapper userMapper, NotificationMapper notificationMapper, tech.cspioneer.backend.mapper.OrgInviteLinkMapper inviteLinkMapper) {
        this.organizationMapper = organizationMapper;
        this.lnkMapper = lnkMapper;
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
        this.inviteLinkMapper = inviteLinkMapper;
    }

    public static class Page<T> {
        public final java.util.List<T> items;
        public final long total;
        public Page(java.util.List<T> items, long total) { this.items = items; this.total = total; }
    }

    public User requireUserByUuid(String userUuid) {
        User u = userMapper.findByUuid(userUuid);
        if (u == null) throw new ApiException(1005, "用户不存在或状态异常");
        return u;
    }

    @Transactional
    public Organization createOrganization(String userUuid, String name, String avatar, String description,
                                           String freeText,
                                           Boolean isPublic, Boolean allowJoinRequest, Boolean allowInviteLink) {
        // 组织Key不允许创建组织
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key不允许创建组织");
        }
        if (organizationMapper.findByName(name) != null) {
            throw new ApiException(1006, "组织名称已存在");
        }
        User user = requireUserByUuid(userUuid);
        Organization org = new Organization();
        org.setUuid(UUID.randomUUID().toString());
        org.setName(name);
        org.setAvatar(avatar);
        org.setDescription(description);
        org.setFreeText(freeText);
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setIsVerified(Boolean.FALSE);
        org.setRating(0L);
        // 可见性与策略（允许为 null，交给 SQL COALESCE 用默认值）
        org.setIsPublic(isPublic);
        org.setAllowJoinRequest(allowJoinRequest);
        org.setAllowInviteLink(allowInviteLink);
        int n = organizationMapper.insert(org);
        if (n != 1) throw new ApiException(1500, "创建组织失败");
        LnkUserOrganization link = new LnkUserOrganization();
        link.setUuid(UUID.randomUUID().toString());
        link.setOrganizationId(org.getId());
        link.setUserId(user.getId());
        link.setRole(OrganizationRole.ADMIN);
        lnkMapper.insert(link);
        // 返回数据库最新数据，确保 dateCreated 等由 DB 填充的字段不为 null
        return organizationMapper.findByUuid(org.getUuid());
    }

    public Page<OrgInviteView> listMyInvitations(String userUuid, String status, Boolean isRead, String orgUuid, int page, int size) {
        // 组织级 API Key 不支持此端点（个人视图）
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key不支持查询个人邀请");
        }
        User user = requireUserByUuid(userUuid);
        Long orgId = null;
        if (orgUuid != null && !orgUuid.isBlank()) {
            Organization org = organizationMapper.findByUuid(orgUuid);
            if (org == null) throw new ApiException(404, "组织不存在");
            orgId = org.getId();
        }
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = notificationMapper.listOrgInvitesForUser(user.getId(), status, isRead, orgId, limit, offset);
        long total = notificationMapper.countOrgInvitesForUser(user.getId(), status, isRead, orgId);
        return new Page<>(items, total);
    }

    public Page<AdminOrgInviteView> listOrgInvitations(String adminUuid,
                                                       String orgUuid,
                                                       String status,
                                                       String inviterUuid,
                                                       String q,
                                                       int page,
                                                       int size) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid); // 包含范围校验
        User actor = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), actor.getId());

        Long inviterId = null;
        if (inviterUuid != null && !inviterUuid.isBlank()) {
            if ("me".equalsIgnoreCase(inviterUuid.trim())) {
                inviterId = actor.getId();
            } else {
                User inviter = userMapper.findByUuid(inviterUuid);
                if (inviter == null) throw new ApiException(404, "用户不存在");
                inviterId = inviter.getId();
            }
        }

        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = notificationMapper.listOrgInvitesByOrg(org.getId(), status, inviterId, q, limit, offset);
        long total = notificationMapper.countOrgInvitesByOrg(org.getId(), status, inviterId, q);
        return new Page<>(items, total);
    }

    public Organization getByUuid(String uuid) {
        Organization o = organizationMapper.findByUuid(uuid);
        if (o == null) throw new ApiException(404, "组织不存在");
        enforceOrgScopeUuid(o.getUuid());
        return o;
    }

    /**
     * 获取组织用于前端展示，包含可见性控制：
     * - 公开组织：任何已登录用户可查看
     * - 私有组织：仅成员/管理员可查看
     */
    public Organization getForView(String userUuid, String orgUuid) {
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        if (org.getStatus() == OrganizationStatus.DELETED) throw new ApiException(404, "组织不存在");
        // 组织级 API Key 范围校验
        enforceOrgScopeUuid(org.getUuid());
        // 公开组织可见
        if (Boolean.TRUE.equals(org.getIsPublic())) return org;
        // 私有组织：要求为成员
        if (userUuid != null) {
            User user = requireUserByUuid(userUuid);
            LnkUserOrganization link = lnkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
            if (link != null) return org;
        }
        throw new ApiException(404, "组织不存在");
    }

    // 对外提供的管理员校验（按 uuid）
    public void ensureAdminByUuid(String userUuid, String orgUuid) {
        Organization org = getByUuid(orgUuid);
        User user = requireUserByUuid(userUuid);
        if (log.isDebugEnabled()) {
            log.debug("[Org] ensureAdminByUuid orgUuid={} userUuid={} -> orgId={} userId={}", orgUuid, userUuid, org.getId(), user.getId());
        }
        ensureAdmin(org.getId(), user.getId());
    }

    public List<OrgWithRole> listMyOrgs(String userUuid) {
        User user = requireUserByUuid(userUuid);
        return organizationMapper.listByUserId(user.getId());
    }

    public List<OrgMemberView> listMembers(String userUuid, String orgUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        // 必须为登录用户；公开组织允许非成员查看完整成员列表；私有组织仅成员可见
        User user = requireUserByUuid(userUuid);
        if (!Boolean.TRUE.equals(org.getIsPublic())) {
            ensureMember(org.getId(), user.getId());
        }
        return organizationMapper.listMembers(org.getId());
    }

    public java.util.List<tech.cspioneer.backend.dto.PublicOrgMemberView> listPublicMembers(String userUuid, String orgUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        // 仅公开组织允许公开成员列表
        if (!Boolean.TRUE.equals(org.getIsPublic())) {
            throw new ApiException(403, "无权限");
        }
        // 登录校验：若是组织级 API Key 也允许 read scope
        if (userUuid != null && !userUuid.isBlank()) {
            requireUserByUuid(userUuid); // 确保用户存在
        }
        return organizationMapper.listPublicMembers(org.getId());
    }

    @Transactional
    public Organization updateBasic(String userUuid, String orgUuid, String name, String avatar, String description,
                                    String freeText, Boolean isPublic, Boolean allowJoinRequest, Boolean allowInviteLink) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User user = requireUserByUuid(userUuid);
        ensureAdmin(org.getId(), user.getId());
        if (name != null) {
            Organization byName = organizationMapper.findByName(name);
            if (byName != null && !byName.getUuid().equals(orgUuid)) {
                throw new ApiException(1006, "组织名称已存在");
            }
            org.setName(name);
        }
        if (avatar != null) org.setAvatar(avatar);
        if (description != null) org.setDescription(description);
        if (freeText != null) org.setFreeText(freeText);
        if (isPublic != null) org.setIsPublic(isPublic);
        if (allowJoinRequest != null) org.setAllowJoinRequest(allowJoinRequest);
        if (allowInviteLink != null) org.setAllowInviteLink(allowInviteLink);
        organizationMapper.updateBasic(org);
        return organizationMapper.findByUuid(orgUuid);
    }

    public Page<Organization> searchPublic(String q, int page, int size) {
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = organizationMapper.listPublicSearch((q == null || q.isBlank()) ? null : q, limit, offset);
        long total = organizationMapper.countPublicSearch((q == null || q.isBlank()) ? null : q);
        return new Page<>(items, total);
    }

    // ===== Invite Link Flow =====
    @Transactional
    public tech.cspioneer.backend.entity.OrgInviteLink generateInviteLink(String adminUuid, String orgUuid, Integer expiresInDays) {
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        // 组织设置校验
        if (!Boolean.TRUE.equals(org.getAllowInviteLink())) {
            throw new ApiException(403, "该组织未开启邀请链接");
        }
        // 生成唯一 code（短码）
        String code = null;
        for (int i = 0; i < 5; i++) {
            String c = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
            if (inviteLinkMapper.findByCode(c) == null) { code = c; break; }
        }
        if (code == null) throw new ApiException(1500, "生成邀请码失败，请重试");
        var link = new tech.cspioneer.backend.entity.OrgInviteLink();
        link.setUuid(java.util.UUID.randomUUID().toString());
        link.setOrgId(org.getId());
        link.setCode(code);
        link.setCreatedBy(admin.getId());
        link.setExpireTime(expiresInDays != null && expiresInDays > 0 ? java.time.LocalDateTime.now().plusDays(expiresInDays) : null);
        link.setIsActive(true);
        inviteLinkMapper.insert(link);
        return inviteLinkMapper.findByUuid(link.getUuid());
    }

    public java.util.List<tech.cspioneer.backend.entity.OrgInviteLink> listInviteLinks(String adminUuid, String orgUuid) {
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        return inviteLinkMapper.listByOrgId(org.getId());
    }

    @Transactional
    public void revokeInviteLink(String adminUuid, String linkUuid) {
        var link = inviteLinkMapper.findByUuid(linkUuid);
        if (link == null) throw new ApiException(404, "邀请链接不存在");
        Organization org = organizationMapper.findById(link.getOrgId());
        if (org == null) throw new ApiException(404, "组织不存在");
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        inviteLinkMapper.deactivateByUuid(linkUuid);
    }

    @Transactional
    public void applyJoinByInviteCode(String userUuid, String code, String message) {
        // 组织级 API key 不允许使用
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key不支持使用邀请链接");
        }
        var link = inviteLinkMapper.findByCode(code);
        if (link == null || !Boolean.TRUE.equals(link.getIsActive())) {
            throw new ApiException(404, "邀请码无效");
        }
        if (link.getExpireTime() != null && java.time.LocalDateTime.now().isAfter(link.getExpireTime())) {
            throw new ApiException(1006, "邀请码已过期");
        }
        Organization org = organizationMapper.findById(link.getOrgId());
        if (org == null) throw new ApiException(404, "组织不存在");
        User user = requireUserByUuid(userUuid);
        // 已是成员则拒绝
        LnkUserOrganization exist = lnkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
        if (exist != null) throw new ApiException(1006, "您已是该组织成员");
        // 已有ACTIVE申请则拒绝
        Long activeReq = notificationMapper.findActiveJoinRequestId(org.getId(), user.getId());
        if (activeReq != null) throw new ApiException(1006, "已存在待处理的申请");
        // 记录加入申请（通过邀请码）
        Notification n = new Notification();
        n.setUuid(java.util.UUID.randomUUID().toString());
        n.setType(tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_JOIN_REQUEST);
        n.setTargetId(org.getId());
        n.setUserId(user.getId());
        n.setSenderId(null);
        n.setTitle("加入申请（邀请码）");
        String extra = (message == null || message.isBlank()) ? "" : ("：" + message.trim());
        n.setContent("通过邀请码 " + code + extra);
        n.setIsRead(false);
        n.setStatus(tech.cspioneer.backend.enums.NotificationStatus.ACTIVE);
        notificationMapper.insert(n);
        try { notificationMapper.updateActionUrlByUuid(n.getUuid(), "/organizations/" + org.getUuid()); } catch (Exception ignore) {}

        // 通知组织管理员：有新的加入申请（邀请码）
        java.util.List<Long> admins = lnkMapper.listAdminUserIdsByOrgId(org.getId());
        if (admins != null) for (Long adminId : admins) {
            if (adminId == null) continue;
            Notification na = new Notification();
            na.setUuid(java.util.UUID.randomUUID().toString());
            na.setType(NotificationType.ORGANIZATION_JOIN_REQUEST);
            na.setTargetId(org.getId());
            na.setUserId(adminId);
            na.setSenderId(user.getId());
            na.setTitle("新的加入申请");
            na.setContent("用户 " + user.getName() + " 通过邀请码申请加入组织: " + org.getName());
            na.setIsRead(false);
            na.setStatus(NotificationStatus.ACTIVE);
            notificationMapper.insert(na);
            try { notificationMapper.updateActionUrlByUuid(na.getUuid(), "/organizations/" + org.getUuid()); } catch (Exception ignore) {}
        }
    }

    // ===== Join Request Flow =====
    @Transactional
    public void submitJoinRequest(String userUuid, String orgUuid, String message) {
        Organization org = getByUuid(orgUuid); // API key scope enforced inside
        if (!Boolean.TRUE.equals(org.getIsPublic()) || !Boolean.TRUE.equals(org.getAllowJoinRequest())) {
            throw new ApiException(403, "该组织不接受公开加入申请");
        }
        User user = requireUserByUuid(userUuid);
        // 已是成员则拒绝
        LnkUserOrganization exist = lnkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
        if (exist != null) throw new ApiException(1006, "您已是该组织成员");
        // 已有ACTIVE申请则忽略/报错
        Long activeReq = notificationMapper.findActiveJoinRequestId(org.getId(), user.getId());
        if (activeReq != null) throw new ApiException(1006, "已存在待处理的申请");
        // 记录申请
        Notification n = new Notification();
        n.setUuid(java.util.UUID.randomUUID().toString());
        n.setType(tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_JOIN_REQUEST);
        n.setTargetId(org.getId());
        n.setUserId(user.getId());
        n.setSenderId(null);
        n.setTitle("加入申请");
        n.setContent(message == null ? "" : message);
        n.setIsRead(false);
        n.setStatus(tech.cspioneer.backend.enums.NotificationStatus.ACTIVE);
        notificationMapper.insert(n);
        try { notificationMapper.updateActionUrlByUuid(n.getUuid(), "/organizations/" + org.getUuid()); } catch (Exception ignore) {}

        // 通知组织管理员：有新的加入申请
        java.util.List<Long> admins = lnkMapper.listAdminUserIdsByOrgId(org.getId());
        if (admins != null) for (Long adminId : admins) {
            if (adminId == null) continue;
            Notification na = new Notification();
            na.setUuid(java.util.UUID.randomUUID().toString());
            na.setType(NotificationType.ORGANIZATION_JOIN_REQUEST);
            na.setTargetId(org.getId());
            na.setUserId(adminId);
            na.setSenderId(user.getId());
            na.setTitle("新的加入申请");
            na.setContent("用户 " + user.getName() + " 申请加入组织: " + org.getName());
            na.setIsRead(false);
            na.setStatus(NotificationStatus.ACTIVE);
            notificationMapper.insert(na);
            try { notificationMapper.updateActionUrlByUuid(na.getUuid(), "/organizations/" + org.getUuid()); } catch (Exception ignore) {}
        }
    }

    public Page<tech.cspioneer.backend.dto.AdminOrgJoinRequestView> listJoinRequests(String adminUuid, String orgUuid, String status, int page, int size) {
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = notificationMapper.listJoinRequestsByOrg(org.getId(), status, limit, offset);
        long total = notificationMapper.countJoinRequestsByOrg(org.getId(), status);
        return new Page<>(items, total);
    }

    @Transactional
    public void approveJoinRequest(String adminUuid, String requestUuid) {
        Notification n = notificationMapper.findByUuid(requestUuid);
        if (n == null || n.getType() != tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_JOIN_REQUEST || n.getStatus() != tech.cspioneer.backend.enums.NotificationStatus.ACTIVE) {
            throw new ApiException(404, "申请不存在或已处理");
        }
        Organization org = organizationMapper.findById(n.getTargetId());
        if (org == null) throw new ApiException(404, "组织不存在");
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        // 若非成员则加入为 MEMBER
        LnkUserOrganization existing = lnkMapper.findByOrgIdAndUserId(org.getId(), n.getUserId());
        if (existing == null) {
            LnkUserOrganization link = new LnkUserOrganization();
            link.setUuid(java.util.UUID.randomUUID().toString());
            link.setOrganizationId(org.getId());
            link.setUserId(n.getUserId());
            link.setRole(tech.cspioneer.backend.enums.OrganizationRole.MEMBER);
            lnkMapper.insert(link);
        }
        notificationMapper.updateStatus(requestUuid, tech.cspioneer.backend.enums.NotificationStatus.ACCEPTED, true);
    }

    @Transactional
    public void rejectJoinRequest(String adminUuid, String requestUuid) {
        Notification n = notificationMapper.findByUuid(requestUuid);
        if (n == null || n.getType() != tech.cspioneer.backend.enums.NotificationType.ORGANIZATION_JOIN_REQUEST || n.getStatus() != tech.cspioneer.backend.enums.NotificationStatus.ACTIVE) {
            throw new ApiException(404, "申请不存在或已处理");
        }
        Organization org = organizationMapper.findById(n.getTargetId());
        if (org == null) throw new ApiException(404, "组织不存在");
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        notificationMapper.updateStatus(requestUuid, tech.cspioneer.backend.enums.NotificationStatus.REJECTED, true);
    }

    @Transactional
    public void addMember(String userUuid, String orgUuid, String targetLogin, String role) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User actor = requireUserByUuid(userUuid);
        ensureAdmin(org.getId(), actor.getId());
        User target = userMapper.findByEmailOrName(targetLogin);
        if (target == null) throw new ApiException(404, "目标用户不存在");
        LnkUserOrganization existing = lnkMapper.findByOrgIdAndUserId(org.getId(), target.getId());
        if (existing != null) throw new ApiException(1006, "用户已在组织中");
        // 直接添加成员
        LnkUserOrganization link = new LnkUserOrganization();
        link.setUuid(UUID.randomUUID().toString());
        link.setOrganizationId(org.getId());
        link.setUserId(target.getId());
        link.setRole(OrganizationRole.fromCode(role));
        lnkMapper.insert(link);
    }

    @Transactional
    public void sendInvite(String userUuid, String orgUuid, String targetLogin) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        if (org.getStatus() != OrganizationStatus.ACTIVE) {
            throw new ApiException(403, "组织已停用，无法发送邀请");
        }
        User actor = requireUserByUuid(userUuid);
        ensureAdmin(org.getId(), actor.getId());
        User target = userMapper.findByEmailOrName(targetLogin);
        if (target == null) throw new ApiException(404, "目标用户不存在");
        LnkUserOrganization existing = lnkMapper.findByOrgIdAndUserId(org.getId(), target.getId());
        if (existing != null) throw new ApiException(1006, "用户已在组织中");
        Long invId = notificationMapper.findActiveOrgInviteId(org.getId(), target.getId());
        if (invId != null) throw new ApiException(1006, "已存在待处理的邀请");
        Notification n = new Notification();
        n.setUuid(UUID.randomUUID().toString());
        n.setType(NotificationType.ORGANIZATION_INVITE);
        n.setTargetId(org.getId());
        n.setUserId(target.getId());
        n.setSenderId(actor.getId());
        n.setTitle("组织邀请");
        n.setContent("邀请加入组织: " + org.getName());
        n.setIsRead(false);
        n.setStatus(NotificationStatus.ACTIVE);
        notificationMapper.insert(n);
        try { notificationMapper.updateActionUrlByUuid(n.getUuid(), "/org-invitations"); } catch (Exception ignore) {}
    }

    @Transactional
    public void changeMemberRole(String userUuid, String orgUuid, String memberUuid, String role) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User actor = requireUserByUuid(userUuid);
        ensureAdmin(org.getId(), actor.getId());
        User member = requireUserByUuid(memberUuid);
        if (lnkMapper.updateRole(org.getId(), member.getId(), OrganizationRole.fromCode(role)) == 0) {
            throw new ApiException(404, "成员不存在");
        }
    }

    @Transactional
    public void removeMember(String userUuid, String orgUuid, String memberUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User actor = requireUserByUuid(userUuid);
        ensureAdmin(org.getId(), actor.getId());
        User member = requireUserByUuid(memberUuid);
        if (lnkMapper.delete(org.getId(), member.getId()) == 0) {
            throw new ApiException(404, "成员不存在");
        }
    }

    @Transactional
    public void acceptInvitation(String userUuid, String inviteUuid) {
        User user = requireUserByUuid(userUuid);
        Notification n = notificationMapper.findByUuid(inviteUuid);
        if (n == null || n.getType() != NotificationType.ORGANIZATION_INVITE || n.getUserId() == null || !n.getUserId().equals(user.getId()) || n.getStatus() != NotificationStatus.ACTIVE) {
            throw new ApiException(404, "邀请不存在或已处理");
        }
        Organization org = organizationMapper.findById(n.getTargetId());
        if (org == null) throw new ApiException(404, "组织不存在");
        enforceOrgScopeUuid(org.getUuid());
        LnkUserOrganization existing = lnkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
        if (existing == null) {
            LnkUserOrganization link = new LnkUserOrganization();
            link.setUuid(UUID.randomUUID().toString());
            link.setOrganizationId(org.getId());
            link.setUserId(user.getId());
            link.setRole(OrganizationRole.MEMBER);
            lnkMapper.insert(link);
        }
        notificationMapper.updateStatus(inviteUuid, NotificationStatus.ACCEPTED, true);
        // 通知邀请发起人
        if (n.getSenderId() != null) {
            Notification back = new Notification();
            back.setUuid(UUID.randomUUID().toString());
            back.setType(NotificationType.ORGANIZATION_INVITE);
            back.setTargetId(n.getTargetId()); // orgId
            back.setUserId(n.getSenderId());
            back.setSenderId(user.getId());
            back.setTitle("邀请已接受");
            back.setContent(user.getName() + " 已接受加入组织");
            back.setIsRead(false);
            back.setStatus(NotificationStatus.ACTIVE);
            notificationMapper.insert(back);
            try {
                // 使用已查询的 org，避免重复定义变量名
                if (org != null) notificationMapper.updateActionUrlByUuid(back.getUuid(), "/organizations/" + org.getUuid());
            } catch (Exception ignore) {}
        }
    }

    @Transactional
    public void rejectInvitation(String userUuid, String inviteUuid) {
        User user = requireUserByUuid(userUuid);
        Notification n = notificationMapper.findByUuid(inviteUuid);
        if (n == null || n.getType() != NotificationType.ORGANIZATION_INVITE || n.getUserId() == null || !n.getUserId().equals(user.getId()) || n.getStatus() != NotificationStatus.ACTIVE) {
            throw new ApiException(404, "邀请不存在或已处理");
        }
        // 组织范围校验
        Organization org = organizationMapper.findById(n.getTargetId());
        if (org != null) enforceOrgScopeUuid(org.getUuid());
        notificationMapper.updateStatus(inviteUuid, NotificationStatus.REJECTED, true);
        // 通知邀请发起人
        if (n.getSenderId() != null) {
            Notification back = new Notification();
            back.setUuid(UUID.randomUUID().toString());
            back.setType(NotificationType.ORGANIZATION_INVITE);
            back.setTargetId(n.getTargetId());
            back.setUserId(n.getSenderId());
            back.setSenderId(user.getId());
            back.setTitle("邀请已拒绝");
            back.setContent(user.getName() + " 已拒绝加入组织");
            back.setIsRead(false);
            back.setStatus(NotificationStatus.ACTIVE);
            notificationMapper.insert(back);
            try {
                if (org != null) notificationMapper.updateActionUrlByUuid(back.getUuid(), "/organizations/" + org.getUuid());
            } catch (Exception ignore) {}
        }
    }


    private void ensureAdmin(Long orgId, Long userId) {
        LnkUserOrganization link = lnkMapper.findByOrgIdAndUserId(orgId, userId);
        if (link == null) {
            log.warn("[Org] ensureAdmin failed: no link found orgId={} userId={}", orgId, userId);
            throw new ApiException(403, "无权限");
        }
        if (link.getRole() != OrganizationRole.ADMIN) {
            log.warn("[Org] ensureAdmin failed: role not admin orgId={} userId={} role={}", orgId, userId, link.getRole());
            throw new ApiException(403, "无权限");
        }
        if (log.isDebugEnabled()) {
            log.debug("[Org] ensureAdmin ok orgId={} userId={} role={} ", orgId, userId, link.getRole());
        }
    }

    private void ensureMember(Long orgId, Long userId) {
        LnkUserOrganization link = lnkMapper.findByOrgIdAndUserId(orgId, userId);
        if (link == null) {
            throw new ApiException(403, "无权限");
        }
    }

    private void enforceOrgScopeUuid(String orgUuid) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            if (ctx.getOrgUuid() != null && !ctx.getOrgUuid().equals(orgUuid)) {
                log.warn("[Org] API key org scope mismatch: keyOrgUuid={} requestOrgUuid={}", ctx.getOrgUuid(), orgUuid);
                throw new ApiException(1012, "组织Key无权操作其他组织");
            }
        }
    }

    @Transactional
    public void disbandOrganization(String adminUuid, String orgUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        if (org.getStatus() != OrganizationStatus.ACTIVE) {
            throw new ApiException(1006, "组织当前状态不允许解散");
        }
        // 1) 更新组织状态与策略
        organizationMapper.updateStatusAndPoliciesByUuid(orgUuid, OrganizationStatus.SUSPENDED.name(), false, false);
        // 2) 撤销所有邀请链接
        inviteLinkMapper.deactivateAllByOrgId(org.getId());
        // 3) 过期所有待处理的邀请与加入申请
        notificationMapper.expireActiveInvitesByOrg(org.getId());
        notificationMapper.expireActiveJoinRequestsByOrg(org.getId());
    }

    // === 新增：暂停/恢复/删除 ===
    @Transactional
    public void suspendOrganization(String adminUuid, String orgUuid) {
        // 暂停组织：语义等价于原“解散”
        disbandOrganization(adminUuid, orgUuid);
    }

    @Transactional
    public void restoreOrganization(String adminUuid, String orgUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        if (org.getStatus() != OrganizationStatus.SUSPENDED) {
            throw new ApiException(1006, "仅暂停状态可恢复");
        }
        // 仅恢复状态为 ACTIVE；allow* 策略由管理员在设置里手动调整
        organizationMapper.updateStatusAndPoliciesByUuid(orgUuid, OrganizationStatus.ACTIVE.name(), null, null);
    }

    @Transactional
    public void deleteOrganization(String adminUuid, String orgUuid) {
        enforceOrgScopeUuid(orgUuid);
        Organization org = getByUuid(orgUuid);
        User admin = requireUserByUuid(adminUuid);
        ensureAdmin(org.getId(), admin.getId());
        if (org.getStatus() != OrganizationStatus.SUSPENDED) {
            throw new ApiException(1006, "删除前请先暂停组织");
        }
        // 生成新名称，释放原名称
        String shortCode = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toLowerCase();
        String newName = org.getName() + "--deleted-" + shortCode;
        // 更新名称与状态
        int n = organizationMapper.updateNameAndStatusByUuid(orgUuid, newName, OrganizationStatus.DELETED.name());
        if (n != 1) throw new ApiException(1500, "删除组织失败");
        // 保险起见，撤销邀请链接/过期待处理申请
        inviteLinkMapper.deactivateAllByOrgId(org.getId());
        notificationMapper.expireActiveInvitesByOrg(org.getId());
        notificationMapper.expireActiveJoinRequestsByOrg(org.getId());
    }
}
