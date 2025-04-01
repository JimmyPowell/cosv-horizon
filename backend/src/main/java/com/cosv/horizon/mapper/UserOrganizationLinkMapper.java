package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.UserOrganizationLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户组织关联数据访问层
 */
@Mapper
public interface UserOrganizationLinkMapper {
    
    /**
     * 插入用户组织关联记录
     * @param link 关联实体
     * @return 影响的行数
     */
    int insert(UserOrganizationLink link);
    
    /**
     * 更新用户在组织中的角色
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @param role 新角色
     * @return 影响的行数
     */
    int updateRole(@Param("organizationId") Long organizationId, @Param("userId") Long userId, @Param("role") String role);
    
    /**
     * 删除用户组织关联
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @return 影响的行数
     */
    int delete(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
    
    /**
     * 删除组织的所有成员关联
     * @param organizationId 组织ID
     * @return 影响的行数
     */
    int deleteByOrganizationId(@Param("organizationId") Long organizationId);
    
    /**
     * 查询用户在组织中的角色
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @return 关联记录
     */
    UserOrganizationLink findByOrganizationAndUser(@Param("organizationId") Long organizationId, @Param("userId") Long userId);
    
    /**
     * 查询组织的所有成员
     * @param organizationId 组织ID
     * @return 成员列表
     */
    List<UserOrganizationLink> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    /**
     * 查询用户所属的所有组织
     * @param userId 用户ID
     * @return 组织ID列表
     */
    List<Long> findOrganizationIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户所属的所有组织（包含组织信息）
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> findOrganizationsByUserId(@Param("userId") Long userId);
} 