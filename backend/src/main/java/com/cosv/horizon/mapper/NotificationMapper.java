package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知数据访问接口
 */
@Mapper
public interface NotificationMapper {
    /**
     * 创建新通知
     *
     * @param notification 通知对象
     * @return 影响的行数
     */
    int insert(Notification notification);

    /**
     * 批量创建通知
     *
     * @param notifications 通知对象列表
     * @return 影响的行数
     */
    int batchInsert(List<Notification> notifications);

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知对象
     */
    Notification selectById(Long id);

    /**
     * 根据用户ID查询通知列表
     *
     * @param userId 用户ID
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 通知列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId, 
                                      @Param("limit") int limit, 
                                      @Param("offset") int offset);

    /**
     * 根据用户ID和通知类型查询通知列表
     *
     * @param userId 用户ID
     * @param type 通知类型
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 通知列表
     */
    List<Notification> selectByUserIdAndType(@Param("userId") Long userId, 
                                            @Param("type") String type, 
                                            @Param("limit") int limit, 
                                            @Param("offset") int offset);

    /**
     * 查询用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    int countUnreadByUserId(Long userId);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 影响的行数
     */
    int markAsRead(Long id);

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @return 影响的行数
     */
    int batchMarkAsRead(List<Long> ids);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    int markAllAsRead(Long userId);

    /**
     * 更新通知状态
     *
     * @param id 通知ID
     * @param status 状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除通知（逻辑删除，将状态设为DELETED）
     *
     * @param id 通知ID
     * @return 影响的行数
     */
    int delete(Long id);

    /**
     * 根据目标ID和类型查询通知
     *
     * @param targetId 目标ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> selectByTargetIdAndType(@Param("targetId") Long targetId, 
                                              @Param("type") String type);

    /**
     * 查询用户通知总数
     *
     * @param userId 用户ID
     * @return 通知总数
     */
    int countByUserId(Long userId);
} 