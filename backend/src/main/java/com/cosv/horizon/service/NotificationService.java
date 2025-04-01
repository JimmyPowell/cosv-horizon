package com.cosv.horizon.service;

import com.cosv.horizon.entity.Notification;
import com.cosv.horizon.entity.response.NotificationResponse;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    /**
     * 创建通知
     *
     * @param notification 通知对象
     * @return 通知ID
     */
    Long createNotification(Notification notification);

    /**
     * 批量创建通知
     *
     * @param notifications 通知对象列表
     * @return 成功创建的数量
     */
    int batchCreateNotifications(List<Notification> notifications);

    /**
     * 创建组织审核通知
     *
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @param approved 是否审核通过
     * @return 通知ID
     */
    Long createOrganizationReviewNotification(Long organizationId, Long userId, boolean approved);

    /**
     * 创建组织转让请求通知
     *
     * @param organizationId 组织ID
     * @param fromUserId 转让人ID
     * @param toUserId 接收人ID
     * @return 通知ID
     */
    Long createOrganizationTransferRequestNotification(Long organizationId, Long fromUserId, Long toUserId);
    
    /**
     * 创建组织转让结果通知
     *
     * @param organizationId 组织ID
     * @param fromUserId 转让人ID
     * @param toUserId 接收人ID
     * @param accepted 是否接受
     * @return 通知ID
     */
    Long createOrganizationTransferResultNotification(Long organizationId, Long fromUserId, Long toUserId, boolean accepted);

    /**
     * 创建组织成员邀请通知
     *
     * @param organizationId 组织ID
     * @param inviterId 邀请人ID
     * @param inviteeId 被邀请人ID
     * @return 通知ID
     */
    Long createOrganizationMemberInviteNotification(Long organizationId, Long inviterId, Long inviteeId);

    /**
     * 创建组织角色变更通知
     *
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 通知ID
     */
    Long createOrganizationRoleChangeNotification(Long organizationId, Long userId, String newRole);

    /**
     * 获取通知详情
     *
     * @param notificationId 通知ID
     * @return 通知响应对象
     */
    NotificationResponse getNotification(Long notificationId);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 通知响应对象列表
     */
    List<NotificationResponse> getUserNotifications(Long userId, int page, int size);

    /**
     * 获取用户指定类型的通知列表
     *
     * @param userId 用户ID
     * @param type 通知类型
     * @param page 页码
     * @param size 每页大小
     * @return 通知响应对象列表
     */
    List<NotificationResponse> getUserNotificationsByType(Long userId, String type, int page, int size);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId);

    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     * @return 成功标记的数量
     */
    int batchMarkAsRead(List<Long> notificationIds);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 成功标记的数量
     */
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId);

    /**
     * 获取通知总数
     *
     * @param userId 用户ID
     * @return 通知总数
     */
    int getNotificationCount(Long userId);
} 