package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 组织数据访问层
 */
@Mapper
public interface OrganizationMapper {
    
    /**
     * 插入新组织
     * @param organization 组织实体
     * @return 影响的行数
     */
    int insert(Organization organization);
    
    /**
     * 按ID查询组织
     * @param id 组织ID
     * @return 组织实体
     */
    Organization findById(@Param("id") Long id);
    
    /**
     * 按名称查询组织
     * @param name 组织名称
     * @return 组织实体
     */
    Organization findByName(@Param("name") String name);
    
    /**
     * 更新组织基本信息
     * @param organization 组织实体
     * @return 影响的行数
     */
    int update(Organization organization);
    
    /**
     * 更新组织状态
     * @param id 组织ID
     * @param status 新状态
     * @param updateDate 更新日期
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updateDate") Date updateDate);
    
    /**
     * 删除组织
     * @param id 组织ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 按状态查询组织列表
     * @param status 组织状态
     * @return 组织列表
     */
    List<Organization> findByStatus(@Param("status") String status);
    
    /**
     * 查询用户创建的组织数量（在指定时间之后）
     * @param userId 用户ID
     * @param startTime 开始时间
     * @return 组织数量
     */
    int countUserCreatedOrganizations(@Param("userId") Long userId, @Param("startTime") Date startTime);
    
    /**
     * 添加审核结果信息
     * @param id 组织ID
     * @param status 新状态（ACTIVE或REJECTED）
     * @param rejectReason 拒绝原因（如适用）
     * @param reviewDate 审核日期
     * @param reviewedBy 审核者ID
     * @return 影响的行数
     */
    int addReviewInfo(
            @Param("id") Long id, 
            @Param("status") String status,
            @Param("rejectReason") String rejectReason,
            @Param("reviewDate") Date reviewDate,
            @Param("reviewedBy") Long reviewedBy
    );
    
    /**
     * 查询所有活跃状态的组织
     * @return 组织列表
     */
    List<Organization> findAllActiveOrganizations();
} 