package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.Notification;
import com.cosv.horizon.entity.NotificationStatus;
import com.cosv.horizon.entity.NotificationType;
import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.response.NotificationResponse;
import com.cosv.horizon.mapper.NotificationMapper;
import com.cosv.horizon.mapper.OrganizationMapper;
import com.cosv.horizon.mapper.UserMapper;
import com.cosv.horizon.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper, UserMapper userMapper, OrganizationMapper organizationMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
    }

    @Override
    @Transactional
    public Long createNotification(Notification notification) {
        try {
            notificationMapper.insert(notification);
            logger.info("创建通知成功: {}", notification);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int batchCreateNotifications(List<Notification> notifications) {
        try {
            if (notifications == null || notifications.isEmpty()) {
                return 0;
            }
            int result = notificationMapper.batchInsert(notifications);
            logger.info("批量创建通知成功，数量: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("批量创建通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long createOrganizationReviewNotification(Long organizationId, Long userId, boolean approved) {
        try {
            Organization organization = organizationMapper.selectById(organizationId);
            if (organization == null) {
                logger.error("创建组织审核通知失败：组织不存在, ID: {}", organizationId);
                return null;
            }

            String title, content;
            if (approved) {
                title = "组织审核通过";
                content = String.format("恭喜！您的组织 \"%s\" 已通过审核，现在可以开始使用了。", organization.getName());
            } else {
                title = "组织审核未通过";
                content = String.format("很遗憾，您的组织 \"%s\" 未通过审核，请修改后重新提交。", organization.getName());
            }

            Notification notification = new Notification(
                    NotificationType.ORGANIZATION_REVIEW.name(),
                    organizationId,
                    userId,
                    title,
                    content
            );
            notification.setActionUrl("/organizations/" + organizationId);
            
            notificationMapper.insert(notification);
            logger.info("创建组织审核通知成功: 组织ID={}, 用户ID={}, 审核结果={}", organizationId, userId, approved);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建组织审核通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long createOrganizationTransferRequestNotification(Long organizationId, Long fromUserId, Long toUserId) {
        try {
            Organization organization = organizationMapper.selectById(organizationId);
            User fromUser = userMapper.selectById(fromUserId);
            
            if (organization == null || fromUser == null) {
                logger.error("创建组织转让请求通知失败：组织或用户不存在, 组织ID: {}, 转让人ID: {}", organizationId, fromUserId);
                return null;
            }

            String title = "组织所有权转让请求";
            String content = String.format("用户 %s 想要将组织 \"%s\" 的所有权转让给您，请确认是否接受。", 
                    fromUser.getUsername(), organization.getName());

            Notification notification = new Notification(
                    NotificationType.ORGANIZATION_TRANSFER_REQUEST.name(),
                    organizationId,
                    toUserId,
                    fromUserId,
                    title,
                    content
            );
            notification.setActionUrl("/organizations/transfer-requests");
            
            notificationMapper.insert(notification);
            logger.info("创建组织转让请求通知成功: 组织ID={}, 从用户ID={}, 到用户ID={}", organizationId, fromUserId, toUserId);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建组织转让请求通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long createOrganizationTransferResultNotification(Long organizationId, Long fromUserId, Long toUserId, boolean accepted) {
        try {
            Organization organization = organizationMapper.selectById(organizationId);
            User toUser = userMapper.selectById(toUserId);
            
            if (organization == null || toUser == null) {
                logger.error("创建组织转让结果通知失败：组织或用户不存在, 组织ID: {}, 接收人ID: {}", organizationId, toUserId);
                return null;
            }

            String title, content;
            if (accepted) {
                title = "组织转让请求已接受";
                content = String.format("用户 %s 已接受您对组织 \"%s\" 的所有权转让请求。", 
                        toUser.getUsername(), organization.getName());
            } else {
                title = "组织转让请求已拒绝";
                content = String.format("用户 %s 已拒绝您对组织 \"%s\" 的所有权转让请求。", 
                        toUser.getUsername(), organization.getName());
            }

            Notification notification = new Notification(
                    NotificationType.ORGANIZATION_TRANSFER_REQUEST.name(),
                    organizationId,
                    fromUserId,
                    toUserId,
                    title,
                    content
            );
            notification.setActionUrl("/organizations/" + organizationId);
            
            notificationMapper.insert(notification);
            logger.info("创建组织转让结果通知成功: 组织ID={}, 从用户ID={}, 到用户ID={}, 结果={}", 
                    organizationId, fromUserId, toUserId, accepted);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建组织转让结果通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long createOrganizationMemberInviteNotification(Long organizationId, Long inviterId, Long inviteeId) {
        try {
            Organization organization = organizationMapper.selectById(organizationId);
            User inviter = userMapper.selectById(inviterId);
            
            if (organization == null || inviter == null) {
                logger.error("创建组织成员邀请通知失败：组织或用户不存在, 组织ID: {}, 邀请人ID: {}", organizationId, inviterId);
                return null;
            }

            String title = "组织成员邀请";
            String content = String.format("用户 %s 邀请您加入组织 \"%s\"，请确认是否接受。", 
                    inviter.getUsername(), organization.getName());

            Notification notification = new Notification(
                    NotificationType.ORGANIZATION_MEMBER_INVITE.name(),
                    organizationId,
                    inviteeId,
                    inviterId,
                    title,
                    content
            );
            notification.setActionUrl("/organizations/invitations");
            
            notificationMapper.insert(notification);
            logger.info("创建组织成员邀请通知成功: 组织ID={}, 邀请人ID={}, 被邀请人ID={}", organizationId, inviterId, inviteeId);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建组织成员邀请通知失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Long createOrganizationRoleChangeNotification(Long organizationId, Long userId, String newRole) {
        try {
            Organization organization = organizationMapper.selectById(organizationId);
            
            if (organization == null) {
                logger.error("创建组织角色变更通知失败：组织不存在, 组织ID: {}", organizationId);
                return null;
            }

            String title = "组织角色变更";
            String content = String.format("您在组织 \"%s\" 中的角色已变更为 %s。", 
                    organization.getName(), newRole);

            Notification notification = new Notification(
                    NotificationType.ORGANIZATION_ROLE_CHANGE.name(),
                    organizationId,
                    userId,
                    title,
                    content
            );
            notification.setActionUrl("/organizations/" + organizationId);
            
            notificationMapper.insert(notification);
            logger.info("创建组织角色变更通知成功: 组织ID={}, 用户ID={}, 新角色={}", organizationId, userId, newRole);
            return notification.getId();
        } catch (Exception e) {
            logger.error("创建组织角色变更通知失败", e);
            throw e;
        }
    }

    @Override
    public NotificationResponse getNotification(Long notificationId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                logger.warn("获取通知详情失败：通知不存在, ID: {}", notificationId);
                return null;
            }
            return convertToResponse(notification);
        } catch (Exception e) {
            logger.error("获取通知详情失败", e);
            throw e;
        }
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<Notification> notifications = notificationMapper.selectByUserId(userId, size, offset);
            return convertToResponseList(notifications);
        } catch (Exception e) {
            logger.error("获取用户通知列表失败", e);
            throw e;
        }
    }

    @Override
    public List<NotificationResponse> getUserNotificationsByType(Long userId, String type, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<Notification> notifications = notificationMapper.selectByUserIdAndType(userId, type, size, offset);
            return convertToResponseList(notifications);
        } catch (Exception e) {
            logger.error("获取用户指定类型通知列表失败", e);
            throw e;
        }
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        try {
            return notificationMapper.countUnreadByUserId(userId);
        } catch (Exception e) {
            logger.error("获取用户未读通知数量失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId) {
        try {
            int result = notificationMapper.markAsRead(notificationId);
            logger.info("标记通知为已读: {}, 结果: {}", notificationId, result > 0);
            return result > 0;
        } catch (Exception e) {
            logger.error("标记通知为已读失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int batchMarkAsRead(List<Long> notificationIds) {
        try {
            if (notificationIds == null || notificationIds.isEmpty()) {
                return 0;
            }
            int result = notificationMapper.batchMarkAsRead(notificationIds);
            logger.info("批量标记通知为已读, 数量: {}, 结果: {}", notificationIds.size(), result);
            return result;
        } catch (Exception e) {
            logger.error("批量标记通知为已读失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        try {
            int result = notificationMapper.markAllAsRead(userId);
            logger.info("标记用户所有通知为已读, 用户ID: {}, 结果: {}", userId, result);
            return result;
        } catch (Exception e) {
            logger.error("标记用户所有通知为已读失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId) {
        try {
            int result = notificationMapper.delete(notificationId);
            logger.info("删除通知: {}, 结果: {}", notificationId, result > 0);
            return result > 0;
        } catch (Exception e) {
            logger.error("删除通知失败", e);
            throw e;
        }
    }

    @Override
    public int getNotificationCount(Long userId) {
        try {
            return notificationMapper.countByUserId(userId);
        } catch (Exception e) {
            logger.error("获取用户通知总数失败", e);
            throw e;
        }
    }

    /**
     * 将通知实体转换为响应对象
     */
    private NotificationResponse convertToResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setTargetId(notification.getTargetId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setRead(notification.isRead());
        response.setCreateTime(notification.getCreateTime());
        response.setActionUrl(notification.getActionUrl());
        
        // 如果有发送者，获取发送者信息
        if (notification.getSenderId() != null) {
            User sender = userMapper.selectById(notification.getSenderId());
            if (sender != null) {
                NotificationResponse.SenderInfo senderInfo = new NotificationResponse.SenderInfo(
                        sender.getId(), 
                        sender.getUsername(), 
                        sender.getAvatarUrl()
                );
                response.setSender(senderInfo);
            }
        }
        
        return response;
    }

    /**
     * 将通知实体列表转换为响应对象列表
     */
    private List<NotificationResponse> convertToResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return new ArrayList<>();
        }
        
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
} 