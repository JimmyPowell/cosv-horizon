package tech.cspioneer.backend.service;

import org.springframework.stereotype.Service;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.mapper.NotificationMapper;
import tech.cspioneer.backend.mapper.UserMapper;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public NotificationService(NotificationMapper notificationMapper, UserMapper userMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
    }

    public List<Notification> list(String userUuid, String type, String status, Boolean isRead, int page, int size) {
        // ORG Key 不允许访问个人通知
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        int limit = Math.max(1, Math.min(100, size <= 0 ? 20 : size));
        int offset = Math.max(0, page <= 0 ? 0 : (page - 1) * limit);
        return notificationMapper.listByUser(user.getId(), emptyToNull(type), emptyToNull(status), isRead, limit, offset);
    }

    public void markRead(String userUuid, String notificationUuid) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        int n = notificationMapper.markReadByUuidAndUser(notificationUuid, user.getId());
        if (n == 0) throw new ApiException(404, "通知不存在");
    }

    public long count(String userUuid, String type, String status, Boolean isRead) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        return notificationMapper.countByUser(user.getId(), emptyToNull(type), emptyToNull(status), isRead);
    }

    public long unreadCount(String userUuid) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        return notificationMapper.countByUser(user.getId(), null, null, false);
    }

    public void markAllRead(String userUuid) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        notificationMapper.markAllReadByUser(user.getId());
    }

    public void delete(String userUuid, String notificationUuid) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof tech.cspioneer.backend.security.ApiKeyContext ctx && "ORG".equalsIgnoreCase(ctx.getSubjectType())) {
            throw new ApiException(1012, "组织Key无权访问个人通知");
        }
        User user = requireUser(userUuid);
        Notification n = notificationMapper.findByUuid(notificationUuid);
        if (n == null || n.getUserId() == null || !n.getUserId().equals(user.getId())) {
            throw new ApiException(404, "通知不存在");
        }
        notificationMapper.updateStatus(notificationUuid, tech.cspioneer.backend.enums.NotificationStatus.DELETED, true);
    }

    public void setActionUrl(String notificationUuid, String actionUrl) {
        notificationMapper.updateActionUrlByUuid(notificationUuid, actionUrl);
    }

    private User requireUser(String userUuid) {
        User user = userMapper.findByUuid(userUuid);
        if (user == null) throw new ApiException(1005, "用户不存在");
        return user;
    }

    private String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
