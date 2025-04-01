package com.cosv.horizon.service;

import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.UserOrganizationLink;
import com.cosv.horizon.entity.request.OrganizationCreateRequest;
import com.cosv.horizon.entity.response.OrganizationResponse;

import java.util.List;

/**
 * 组织服务接口
 * 处理组织的创建、修改、删除等操作
 */
public interface OrganizationService {
    
    /**
     * 创建新组织
     * @param request 组织创建请求
     * @param creatorId 创建者用户ID
     * @return 创建的组织实体
     */
    Organization createOrganization(OrganizationCreateRequest request, Long creatorId);
    
    /**
     * 更新组织基本信息
     * @param id 组织ID
     * @param request 组织信息更新请求
     * @param userId 操作用户ID
     * @return 更新后的组织
     */
    Organization updateOrganization(Long id, OrganizationCreateRequest request, Long userId);
    
    /**
     * 更改组织状态
     * @param id 组织ID
     * @param status 新状态
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean updateOrganizationStatus(Long id, String status, Long userId);
    
    /**
     * 删除组织
     * @param id 组织ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean deleteOrganization(Long id, Long userId);
    
    /**
     * 获取组织详情
     * @param id 组织ID
     * @return 组织详情
     */
    Organization getOrganizationById(Long id);
    
    /**
     * 获取用户所属的所有组织
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> getUserOrganizations(Long userId);
    
    /**
     * 检查用户是否有权限修改组织
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasEditPermission(Long organizationId, Long userId);
    
    /**
     * 获取组织的所有成员
     * @param organizationId 组织ID
     * @return 成员列表
     */
    List<UserOrganizationLink> getOrganizationMembers(Long organizationId);
    
    /**
     * 获取待审核的组织列表
     * @return 待审核组织列表
     */
    List<Organization> getPendingOrganizations();
    
    /**
     * 审核组织（管理员操作）
     * @param id 组织ID
     * @param approved 是否批准
     * @param rejectReason 拒绝原因（如果不批准）
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean reviewOrganization(Long id, boolean approved, String rejectReason, Long adminId);
    
    /**
     * 将组织数据转换为响应对象
     * @param organization 组织实体
     * @param currentUserId 当前查询用户ID
     * @return 组织响应对象
     */
    OrganizationResponse convertToResponse(Organization organization, Long currentUserId);
    
    /**
     * 获取所有活跃状态组织
     * @return 组织列表
     */
    List<Organization> getAllActiveOrganizations();
    
    /**
     * 获取公开组织信息（只返回已激活的组织）
     * @param id 组织ID
     * @return 组织实体，不存在或非激活状态返回null
     */
    Organization getPublicOrganizationById(Long id);
} 