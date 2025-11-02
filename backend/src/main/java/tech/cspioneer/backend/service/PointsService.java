package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.*;
import tech.cspioneer.backend.mapper.*;
import tech.cspioneer.backend.enums.OrganizationRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PointsService {
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final UserPointsLedgerMapper userPointsLedgerMapper;
    private final OrgPointsLedgerMapper orgPointsLedgerMapper;
    private final LnkUserOrganizationMapper lnkUserOrganizationMapper;

    public PointsService(UserMapper userMapper, OrganizationMapper organizationMapper,
                         UserPointsLedgerMapper userPointsLedgerMapper, OrgPointsLedgerMapper orgPointsLedgerMapper,
                         LnkUserOrganizationMapper lnkUserOrganizationMapper) {
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.userPointsLedgerMapper = userPointsLedgerMapper;
        this.orgPointsLedgerMapper = orgPointsLedgerMapper;
        this.lnkUserOrganizationMapper = lnkUserOrganizationMapper;
    }

    @Transactional
    public void addUserPoints(String userUuid, int delta, String reason, String refType, String refId, String idempotencyKey) {
        User user = userMapper.findByUuid(userUuid);
        if (user == null) throw new ApiException(404, "用户不存在");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer exists = userPointsLedgerMapper.existsByIdem(user.getId(), idempotencyKey);
            if (exists != null) return; // 幂等：已存在则忽略
        }
        UserPointsLedger l = new UserPointsLedger();
        l.setUuid(UUID.randomUUID().toString());
        l.setUserId(user.getId());
        l.setDelta(delta);
        l.setReason(reason);
        l.setRefType(refType);
        l.setRefId(refId);
        l.setIdempotencyKey(idempotencyKey);
        userPointsLedgerMapper.insert(l);
        userMapper.incrementRating(user.getId(), delta);
    }

    @Transactional
    public void addOrgPoints(String orgUuid, int delta, String reason, String refType, String refId, String idempotencyKey) {
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Integer exists = orgPointsLedgerMapper.existsByIdem(org.getId(), idempotencyKey);
            if (exists != null) return;
        }
        OrgPointsLedger l = new OrgPointsLedger();
        l.setUuid(UUID.randomUUID().toString());
        l.setOrganizationId(org.getId());
        l.setDelta(delta);
        l.setReason(reason);
        l.setRefType(refType);
        l.setRefId(refId);
        l.setIdempotencyKey(idempotencyKey);
        orgPointsLedgerMapper.insert(l);
        organizationMapper.incrementRating(org.getId(), delta);
    }

    public static class Page<T> {
        public final List<T> items;
        public final long total;
        public Page(List<T> items, long total) { this.items = items; this.total = total; }
    }

    public Page<UserPointsLedger> listUserPoints(String userUuid, int page, int size) {
        User user = userMapper.findByUuid(userUuid);
        if (user == null) throw new ApiException(404, "用户不存在");
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = userPointsLedgerMapper.listByUser(user.getId(), limit, offset);
        long total = userPointsLedgerMapper.countByUser(user.getId());
        return new Page<>(items, total);
    }

    public Page<OrgPointsLedger> listOrgPoints(String userUuid, String orgUuid, int page, int size) {
        // 权限：仅组织管理员可查
        Organization org = organizationMapper.findByUuid(orgUuid);
        if (org == null) throw new ApiException(404, "组织不存在");
        User user = userMapper.findByUuid(userUuid);
        if (user == null) throw new ApiException(404, "用户不存在");
        LnkUserOrganization link = lnkUserOrganizationMapper.findByOrgIdAndUserId(org.getId(), user.getId());
        if (link == null || link.getRole() != OrganizationRole.ADMIN) {
            throw new ApiException(403, "无权限");
        }
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        var items = orgPointsLedgerMapper.listByOrg(org.getId(), limit, offset);
        long total = orgPointsLedgerMapper.countByOrg(org.getId());
        return new Page<>(items, total);
    }

    // ===== Summaries =====
    public Map<String, Object> userSummary(String userUuid) {
        User u = userMapper.findByUuid(userUuid);
        if (u == null) throw new ApiException(404, "用户不存在");
        long rating = u.getRating() == null ? 0L : u.getRating();
        long rank = userMapper.rankByRating(rating);
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("uuid", u.getUuid());
        m.put("rating", rating);
        m.put("rank", rank);
        return m;
    }

    public Map<String, Object> orgSummary(String orgUuid) {
        Organization o = organizationMapper.findByUuid(orgUuid);
        if (o == null) throw new ApiException(404, "组织不存在");
        long rating = o.getRating() == null ? 0L : o.getRating();
        long rank = organizationMapper.rankByRating(rating);
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("uuid", o.getUuid());
        m.put("rating", rating);
        m.put("rank", rank);
        return m;
    }
}
