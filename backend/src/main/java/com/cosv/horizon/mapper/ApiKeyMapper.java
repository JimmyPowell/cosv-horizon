package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.ApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * API密钥数据访问接口
 */
@Mapper
public interface ApiKeyMapper {
    /**
     * 插入API密钥
     *
     * @param apiKey API密钥对象
     * @return 影响行数
     */
    int insert(ApiKey apiKey);

    /**
     * 根据哈希值查询有效密钥
     *
     * @param keyHash 密钥哈希值
     * @return API密钥对象
     */
    ApiKey findActiveByKeyHash(@Param("keyHash") String keyHash);

    /**
     * 根据前缀查询密钥（通常仅用于内部管理，避免暴露哈希）
     *
     * @param keyPrefix 密钥前缀
     * @return API密钥对象列表
     */
    List<ApiKey> findByKeyPrefix(@Param("keyPrefix") String keyPrefix);

    /**
     * 根据ID查询密钥
     *
     * @param id 密钥ID
     * @return API密钥对象
     */
    ApiKey findById(@Param("id") Long id);

    /**
     * 查询用户创建的所有密钥
     *
     * @param creatorUserId 用户ID
     * @return 密钥列表
     */
    List<ApiKey> findByCreatorUserId(@Param("creatorUserId") Long creatorUserId);

    /**
     * 查询组织的所有密钥
     *
     * @param organizationId 组织ID
     * @return 密钥列表
     */
    List<ApiKey> findByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * 更新密钥状态
     *
     * @param id 密钥ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新最后使用时间和IP
     *
     * @param id 密钥ID
     * @param lastUsedTime 最后使用时间
     * @param lastUsedIp 最后使用IP
     * @return 影响行数
     */
    int updateLastUsed(@Param("id") Long id, @Param("lastUsedTime") Date lastUsedTime, @Param("lastUsedIp") String lastUsedIp);

    /**
     * 删除密钥（通过更新状态为REVOKED）
     *
     * @param id 密钥ID
     * @return 影响行数
     */
    int revokeById(@Param("id") Long id);

    /**
     * 批量撤销密钥
     *
     * @param ids 密钥ID列表
     * @return 影响行数
     */
    int revokeByIds(@Param("ids") List<Long> ids);
} 