package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.OrganizationRole;
import com.cosv.horizon.entity.OrganizationStatus;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.UserOrganizationLink;
import com.cosv.horizon.entity.request.OrganizationCreateRequest;
import com.cosv.horizon.entity.response.OrganizationResponse;
import com.cosv.horizon.mapper.OrganizationMapper;
import com.cosv.horizon.mapper.UserMapper;
import com.cosv.horizon.mapper.UserOrganizationLinkMapper;
import com.cosv.horizon.service.OrganizationService;
import com.cosv.horizon.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织服务实现
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    
    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private UserOrganizationLinkMapper userOrganizationLinkMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    // 24小时内用户最多可创建的组织数量
    @Value("${organization.max-per-day:3}")
    private int maxOrganizationsPerDay;
    
    @Override
    @Transactional
    public Organization createOrganization(OrganizationCreateRequest request, Long creatorId) {
        // 检查创建限制
        Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        int count = organizationMapper.countUserCreatedOrganizations(creatorId, oneDayAgo);
        if (count >= maxOrganizationsPerDay) {
            logger.warn("用户ID {} 在24小时内创建组织数量已达上限: {}", creatorId, maxOrganizationsPerDay);
            throw new RuntimeException("您在24小时内创建的组织数量已达上限，请稍后再试");
        }
        
        // 检查组织名称是否已存在
        if (organizationMapper.findByName(request.getName()) != null) {
            logger.warn("组织名称已存在: {}", request.getName());
            throw new RuntimeException("组织名称已存在");
        }
        
        // 创建组织对象
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setStatus(OrganizationStatus.PENDING.name()); // 初始状态为待审核
        organization.setDateCreated(new Date());
        organization.setDescription(request.getDescription());
        organization.setAvatar(request.getAvatar());
        organization.setFreeText(request.getFreeText());
        organization.setRating(0L);
        
        // 保存组织
        organizationMapper.insert(organization);
        logger.info("创建组织: {}, ID: {}, 创建者ID: {}", organization.getName(), organization.getId(), creatorId);
        
        // 创建创建者与组织的关联(管理员角色)
        UserOrganizationLink link = new UserOrganizationLink();
        link.setOrganizationId(organization.getId());
        link.setUserId(creatorId);
        link.setRole(OrganizationRole.ADMIN.name());
        userOrganizationLinkMapper.insert(link);
        logger.info("创建用户-组织关联: 用户ID: {}, 组织ID: {}, 角色: {}", creatorId, organization.getId(), link.getRole());
        
        return organization;
    }
    
    @Override
    @Transactional
    public Organization updateOrganization(Long id, OrganizationCreateRequest request, Long userId) {
        // 检查组织是否存在
        Organization organization = organizationMapper.findById(id);
        if (organization == null) {
            logger.warn("组织不存在: {}", id);
            throw new RuntimeException("组织不存在");
        }
        
        // 检查是否有权限修改
        if (!hasEditPermission(id, userId)) {
            logger.warn("用户没有权限修改组织: 用户ID: {}, 组织ID: {}", userId, id);
            throw new RuntimeException("您没有权限修改此组织");
        }
        
        // 如果修改了名称，检查名称是否已被占用
        if (!organization.getName().equals(request.getName())) {
            Organization existingOrg = organizationMapper.findByName(request.getName());
            if (existingOrg != null && !existingOrg.getId().equals(id)) {
                logger.warn("组织名称已存在: {}", request.getName());
                throw new RuntimeException("组织名称已存在");
            }
        }
        
        // 更新组织信息
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setAvatar(request.getAvatar());
        organization.setFreeText(request.getFreeText());
        
        organizationMapper.update(organization);
        logger.info("更新组织: {}, ID: {}, 操作者ID: {}", organization.getName(), organization.getId(), userId);
        
        return organization;
    }
    
    @Override
    @Transactional
    public boolean updateOrganizationStatus(Long id, String status, Long userId) {
        // 检查组织是否存在
        Organization organization = organizationMapper.findById(id);
        if (organization == null) {
            logger.warn("组织不存在: {}", id);
            throw new RuntimeException("组织不存在");
        }
        
        // 检查是否有权限修改
        if (!hasEditPermission(id, userId)) {
            logger.warn("用户没有权限修改组织状态: 用户ID: {}, 组织ID: {}", userId, id);
            throw new RuntimeException("您没有权限修改此组织状态");
        }
        
        // 检查状态是否有效
        if (!OrganizationStatus.isValid(status)) {
            logger.warn("无效的组织状态: {}", status);
            throw new RuntimeException("无效的组织状态");
        }
        
        // 更新状态
        int result = organizationMapper.updateStatus(id, status, new Date());
        logger.info("更新组织状态: 组织ID: {}, 新状态: {}, 操作者ID: {}, 结果: {}", id, status, userId, result > 0);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteOrganization(Long id, Long userId) {
        // 检查组织是否存在
        Organization organization = organizationMapper.findById(id);
        if (organization == null) {
            logger.warn("组织不存在: {}", id);
            throw new RuntimeException("组织不存在");
        }
        
        // 检查是否有权限删除（只有管理员可以删除）
        UserOrganizationLink link = userOrganizationLinkMapper.findByOrganizationAndUser(id, userId);
        if (link == null || !OrganizationRole.ADMIN.name().equals(link.getRole())) {
            logger.warn("用户没有权限删除组织: 用户ID: {}, 组织ID: {}", userId, id);
            throw new RuntimeException("您没有权限删除此组织");
        }
        
        // 删除所有成员关联
        userOrganizationLinkMapper.deleteByOrganizationId(id);
        logger.info("删除组织成员关联: 组织ID: {}", id);
        
        // 删除组织
        int result = organizationMapper.delete(id);
        logger.info("删除组织: ID: {}, 操作者ID: {}, 结果: {}", id, userId, result > 0);
        
        return result > 0;
    }
    
    @Override
    public Organization getOrganizationById(Long id) {
        return organizationMapper.findById(id);
    }
    
    @Override
    public List<Organization> getUserOrganizations(Long userId) {
        return userOrganizationLinkMapper.findOrganizationsByUserId(userId);
    }
    
    @Override
    public boolean hasEditPermission(Long organizationId, Long userId) {
        UserOrganizationLink link = userOrganizationLinkMapper.findByOrganizationAndUser(organizationId, userId);
        return link != null && link.hasEditPrivilege();
    }
    
    @Override
    public List<UserOrganizationLink> getOrganizationMembers(Long organizationId) {
        return userOrganizationLinkMapper.findByOrganizationId(organizationId);
    }
    
    @Override
    public List<Organization> getPendingOrganizations() {
        return organizationMapper.findByStatus(OrganizationStatus.PENDING.name());
    }
    
    @Override
    @Transactional
    public boolean reviewOrganization(Long id, boolean approved, String rejectReason, Long adminId) {
        // 检查组织是否存在
        Organization organization = organizationMapper.findById(id);
        if (organization == null) {
            logger.warn("组织不存在: {}", id);
            throw new RuntimeException("组织不存在");
        }
        
        // 检查组织状态
        if (!OrganizationStatus.PENDING.name().equals(organization.getStatus())) {
            logger.warn("组织状态不是待审核: ID: {}, 当前状态: {}", id, organization.getStatus());
            throw new RuntimeException("只能审核处于待审核状态的组织");
        }
        
        // 更新组织状态
        String newStatus = approved ? OrganizationStatus.ACTIVE.name() : OrganizationStatus.CLOSED.name();
        int result = organizationMapper.addReviewInfo(id, newStatus, approved ? null : rejectReason, new Date(), adminId);
        
        logger.info("审核组织: ID: {}, 结果: {}, 审核者ID: {}, 新状态: {}", id, approved ? "通过" : "拒绝", adminId, newStatus);
        
        // 发送通知给组织创建者
        Long creatorId = getOrganizationCreator(id);
        if (creatorId != null) {
            notificationService.createOrganizationReviewNotification(id, creatorId, approved);
            logger.info("已发送组织审核通知给创建者: 组织ID={}, 创建者ID={}, 审核结果={}", id, creatorId, approved);
        }
        
        return result > 0;
    }
    
    @Override
    public OrganizationResponse convertToResponse(Organization organization, Long currentUserId) {
        if (organization == null) {
            return null;
        }
        
        OrganizationResponse response = new OrganizationResponse(organization);
        
        // 获取成员列表
        List<UserOrganizationLink> members = getOrganizationMembers(organization.getId());
        List<OrganizationResponse.UserRoleInfo> memberInfos = new ArrayList<>();
        
        for (UserOrganizationLink link : members) {
            User user = userMapper.findById(link.getUserId());
            if (user != null) {
                OrganizationResponse.UserRoleInfo userInfo = new OrganizationResponse.UserRoleInfo(user, link.getRole());
                memberInfos.add(userInfo);
                
                // 设置当前用户在组织中的角色
                if (currentUserId != null && user.getId().equals(currentUserId)) {
                    response.setUserRole(link.getRole());
                }
            }
        }
        
        response.setMembers(memberInfos);
        return response;
    }
    
    @Override
    public List<Organization> getAllActiveOrganizations() {
        return organizationMapper.findAllActiveOrganizations();
    }
    
    @Override
    public Organization getPublicOrganizationById(Long id) {
        Organization organization = organizationMapper.findById(id);
        // 只返回活跃状态的组织
        if (organization != null && OrganizationStatus.ACTIVE.name().equals(organization.getStatus())) {
            return organization;
        }
        return null;
    }

    /**
     * 审核组织
     *
     * @param id 组织ID
     * @param status 审核状态
     * @param message 审核消息
     * @return 组织响应对象
     */
    @Override
    @Transactional
    public OrganizationResponse reviewOrganization(Long id, String status, String message) {
        logger.info("审核组织: id={}, status={}, message={}", id, status, message);
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            logger.warn("组织不存在: {}", id);
            throw new EntityNotFoundException("组织不存在");
        }

        if (!Objects.equals(organization.getStatus(), OrganizationStatus.PENDING.name())) {
            logger.warn("组织状态不是待审核: {}, 当前状态: {}", id, organization.getStatus());
            throw new InvalidOperationException("只能审核状态为待审核的组织");
        }

        OrganizationStatus newStatus;
        try {
            newStatus = OrganizationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            logger.warn("无效的组织状态: {}", status);
            throw new InvalidOperationException("无效的组织状态");
        }

        if (newStatus != OrganizationStatus.ACTIVE && newStatus != OrganizationStatus.REJECTED) {
            logger.warn("无效的审核状态: {}", status);
            throw new InvalidOperationException("审核状态只能是ACTIVE或REJECTED");
        }

        organization.setStatus(newStatus.name());
        organization.setReviewMessage(message);
        organization.setReviewTime(new Date());
        organizationMapper.updateStatus(id, newStatus.name(), message);
        logger.info("组织审核完成: id={}, status={}", id, newStatus);

        // 发送通知给组织创建者
        Long creatorId = getOrganizationCreator(id);
        if (creatorId != null) {
            boolean isApproved = OrganizationStatus.ACTIVE.name().equals(status);
            notificationService.createOrganizationReviewNotification(id, creatorId, isApproved);
            logger.info("已发送组织审核通知给创建者: 组织ID={}, 创建者ID={}, 审核结果={}", id, creatorId, isApproved);
        }

        return convertToResponse(organization);
    }

    /**
     * 获取组织创建者ID
     */
    private Long getOrganizationCreator(Long organizationId) {
        UserOrganizationLink adminLink = userOrganizationLinkMapper.selectByOrgIdAndRole(organizationId, UserOrganizationRole.ADMIN.name());
        return adminLink != null ? adminLink.getUserId() : null;
    }
} 