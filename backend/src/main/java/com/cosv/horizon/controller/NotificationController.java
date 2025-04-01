package com.cosv.horizon.controller;

import com.cosv.horizon.entity.response.NotificationResponse;
import com.cosv.horizon.service.NotificationService;
import com.cosv.horizon.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取当前用户的通知列表
     *
     * @param page 页码，默认为1
     * @param size 每页条数，默认为20
     * @return 通知列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<NotificationResponse> notifications = notificationService.getUserNotifications(userId, page, size);
            int total = notificationService.getNotificationCount(userId);
            int unreadCount = notificationService.getUnreadNotificationCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", total);
            response.put("unreadCount", unreadCount);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户通知列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取当前用户的未读通知数量
     *
     * @return 未读通知数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Integer>> getUnreadNotificationCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            int unreadCount = notificationService.getUnreadNotificationCount(userId);
            Map<String, Integer> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户未读通知数量失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取指定类型的通知列表
     *
     * @param type 通知类型
     * @param page 页码，默认为1
     * @param size 每页条数，默认为20
     * @return 通知列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<NotificationResponse> notifications = notificationService.getUserNotificationsByType(userId, type, page, size);
            int total = notificationService.getNotificationCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("type", type);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户指定类型通知列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 结果
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            boolean success = notificationService.markAsRead(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("标记通知为已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @return 结果
     */
    @PostMapping("/batch-read")
    public ResponseEntity<Map<String, Integer>> batchMarkAsRead(@RequestBody List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            int count = notificationService.batchMarkAsRead(ids);
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量标记通知为已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 标记所有通知为已读
     *
     * @return 结果
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            int count = notificationService.markAllAsRead(userId);
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("标记所有通知为已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            boolean success = notificationService.deleteNotification(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除通知失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 