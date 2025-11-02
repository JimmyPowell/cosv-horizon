package tech.cspioneer.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import tech.cspioneer.backend.common.ApiResponse;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.service.NotificationService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@Tag(name = "通知管理")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "我的通知列表")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "type", required = false) String type,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "isRead", required = false) Boolean isRead,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") int size,
                                                 @RequestParam(value = "withTotal", required = false, defaultValue = "false") boolean withTotal,
                                                 Principal principal) {
        List<Notification> items = notificationService.list(principal.getName(), type, status, isRead, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("size", size);
        data.put("items", items.stream().map(this::view).toList());
        if (withTotal) {
            long total = notificationService.count(principal.getName(), type, status, isRead);
            data.put("total", total);
        }
        return ApiResponse.success(data);
    }

    public static class MarkReadReq { @NotBlank public String uuid; }

    @PostMapping("/read")
    @Operation(summary = "标记通知为已读")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Void> markRead(@Valid @RequestBody MarkReadReq req, Principal principal) {
        notificationService.markRead(principal.getName(), req.uuid);
        return ApiResponse.success(null);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数量")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Map<String, Object>> unreadCount(Principal principal) {
        long cnt = notificationService.unreadCount(principal.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("count", cnt);
        return ApiResponse.success(data);
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "全部标记为已读")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Void> markAllRead(Principal principal) {
        notificationService.markAllRead(principal.getName());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "删除通知（软删）")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER','ADMIN') or hasAuthority('SCOPE_notification:read')")
    public ApiResponse<Void> delete(@PathVariable("uuid") String uuid, Principal principal) {
        notificationService.delete(principal.getName(), uuid);
        return ApiResponse.success(null);
    }

    private Map<String, Object> view(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", n.getUuid());
        m.put("type", n.getType());
        m.put("targetId", n.getTargetId());
        m.put("title", n.getTitle());
        m.put("content", n.getContent());
        m.put("isRead", n.getIsRead());
        m.put("createTime", n.getCreateTime());
        m.put("status", n.getStatus());
        m.put("actionUrl", n.getActionUrl());
        return m;
    }
}
