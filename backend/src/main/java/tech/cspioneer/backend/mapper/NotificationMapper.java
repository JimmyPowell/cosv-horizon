package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Notification;
import tech.cspioneer.backend.enums.NotificationStatus;
import tech.cspioneer.backend.dto.OrgInviteView;
import tech.cspioneer.backend.dto.AdminOrgInviteView;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification(uuid, type, target_id, user_id, sender_id, title, content, is_read, create_time, status) VALUES(#{uuid}, #{type}, #{targetId}, #{userId}, #{senderId}, #{title}, #{content}, #{isRead}, NOW(), #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification n);

    @Select("SELECT id, uuid, type, target_id AS targetId, user_id AS userId, sender_id AS senderId, title, content, is_read AS isRead, create_time AS createTime, expire_time AS expireTime, action_url AS actionUrl, status FROM notification WHERE uuid = #{uuid} LIMIT 1")
    Notification findByUuid(@Param("uuid") String uuid);

    @Update("UPDATE notification SET status = #{status}, is_read = #{isRead} WHERE uuid = #{uuid}")
    int updateStatus(@Param("uuid") String uuid, @Param("status") NotificationStatus status, @Param("isRead") boolean isRead);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllReadByUser(@Param("userId") Long userId);

    @Update("UPDATE notification SET action_url = #{actionUrl} WHERE uuid = #{uuid}")
    int updateActionUrlByUuid(@Param("uuid") String uuid, @Param("actionUrl") String actionUrl);

    @Select("SELECT id FROM notification WHERE type = 'ORGANIZATION_INVITE' AND target_id = #{orgId} AND user_id = #{userId} AND status = 'ACTIVE' LIMIT 1")
    Long findActiveOrgInviteId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE uuid = #{uuid} AND user_id = #{userId}")
    int markReadByUuidAndUser(@Param("uuid") String uuid, @Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT id, uuid, type, target_id AS targetId, user_id AS userId, sender_id AS senderId, title, content, is_read AS isRead, create_time AS createTime, expire_time AS expireTime, action_url AS actionUrl, status",
            "FROM notification",
            "WHERE user_id = #{userId}",
            "<if test='type != null'> AND type = #{type} </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "<if test='isRead != null'> AND is_read = #{isRead} </if>",
            "ORDER BY create_time DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<Notification> listByUser(@Param("userId") Long userId,
                                            @Param("type") String type,
                                            @Param("status") String status,
                                            @Param("isRead") Boolean isRead,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM notification",
            "WHERE user_id = #{userId}",
            "<if test='type != null'> AND type = #{type} </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "<if test='isRead != null'> AND is_read = #{isRead} </if>",
            "</script>"
    })
    long countByUser(@Param("userId") Long userId,
                     @Param("type") String type,
                     @Param("status") String status,
                     @Param("isRead") Boolean isRead);

    @Select({
            "<script>",
            "SELECT ",
            "  n.uuid AS inviteUuid,",
            "  n.status AS status,",
            "  n.is_read AS isRead,",
            "  n.create_time AS createTime,",
            "  o.uuid AS orgUuid,",
            "  o.name AS orgName,",
            "  o.avatar AS orgAvatar,",
            "  u.uuid AS inviterUuid,",
            "  u.name AS inviterName,",
            "  u.avatar AS inviterAvatar",
            "FROM notification n",
            "JOIN organization o ON o.id = n.target_id",
            "LEFT JOIN `user` u ON u.id = n.sender_id",
            "WHERE n.user_id = #{userId} AND n.type = 'ORGANIZATION_INVITE'",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "<if test='isRead != null'> AND n.is_read = #{isRead} </if>",
            "<if test='orgId != null'> AND n.target_id = #{orgId} </if>",
            "ORDER BY n.create_time DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<OrgInviteView> listOrgInvitesForUser(@Param("userId") Long userId,
                                                        @Param("status") String status,
                                                        @Param("isRead") Boolean isRead,
                                                        @Param("orgId") Long orgId,
                                                        @Param("limit") int limit,
                                                        @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM notification n",
            "WHERE n.user_id = #{userId} AND n.type = 'ORGANIZATION_INVITE'",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "<if test='isRead != null'> AND n.is_read = #{isRead} </if>",
            "<if test='orgId != null'> AND n.target_id = #{orgId} </if>",
            "</script>"
    })
    long countOrgInvitesForUser(@Param("userId") Long userId,
                                @Param("status") String status,
                                @Param("isRead") Boolean isRead,
                                @Param("orgId") Long orgId);

    @Select({
            "<script>",
            "SELECT ",
            "  n.uuid AS inviteUuid,",
            "  n.status AS status,",
            "  n.is_read AS isRead,",
            "  n.create_time AS createTime,",
            "  ui.uuid AS inviteeUuid,",
            "  ui.name AS inviteeName,",
            "  ui.email AS inviteeEmail,",
            "  ui.avatar AS inviteeAvatar,",
            "  us.uuid AS inviterUuid,",
            "  us.name AS inviterName,",
            "  us.avatar AS inviterAvatar",
            "FROM notification n",
            "JOIN `user` ui ON ui.id = n.user_id",
            "LEFT JOIN `user` us ON us.id = n.sender_id",
            "WHERE n.type = 'ORGANIZATION_INVITE' AND n.target_id = #{orgId}",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "<if test='inviterId != null'> AND n.sender_id = #{inviterId} </if>",
            "<if test=\"q != null and q != ''\"> AND (ui.email LIKE CONCAT('%', #{q}, '%') OR ui.name LIKE CONCAT('%', #{q}, '%')) </if>",
            "ORDER BY n.create_time DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<AdminOrgInviteView> listOrgInvitesByOrg(@Param("orgId") Long orgId,
                                                           @Param("status") String status,
                                                           @Param("inviterId") Long inviterId,
                                                           @Param("q") String q,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM notification n",
            "JOIN `user` ui ON ui.id = n.user_id",
            "WHERE n.type = 'ORGANIZATION_INVITE' AND n.target_id = #{orgId}",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "<if test='inviterId != null'> AND n.sender_id = #{inviterId} </if>",
            "<if test=\"q != null and q != ''\"> AND (ui.email LIKE CONCAT('%', #{q}, '%') OR ui.name LIKE CONCAT('%', #{q}, '%')) </if>",
            "</script>"
    })
    long countOrgInvitesByOrg(@Param("orgId") Long orgId,
                              @Param("status") String status,
                              @Param("inviterId") Long inviterId,
                              @Param("q") String q);

    // ======= Join Requests =======
    @Select("SELECT id FROM notification WHERE type = 'ORGANIZATION_JOIN_REQUEST' AND target_id = #{orgId} AND user_id = #{userId} AND status = 'ACTIVE' LIMIT 1")
    Long findActiveJoinRequestId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT ",
            "  n.uuid AS requestUuid,",
            "  n.status AS status,",
            "  n.is_read AS isRead,",
            "  n.create_time AS createTime,",
            "  ui.uuid AS applicantUuid,",
            "  ui.name AS applicantName,",
            "  ui.email AS applicantEmail,",
            "  ui.avatar AS applicantAvatar,",
            "  n.content AS content",
            "FROM notification n",
            "JOIN `user` ui ON ui.id = n.user_id",
            "WHERE n.type = 'ORGANIZATION_JOIN_REQUEST' AND n.target_id = #{orgId} AND n.sender_id IS NULL",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "ORDER BY n.create_time DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<tech.cspioneer.backend.dto.AdminOrgJoinRequestView> listJoinRequestsByOrg(@Param("orgId") Long orgId,
                                                                                           @Param("status") String status,
                                                                                           @Param("limit") int limit,
                                                                                           @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM notification n",
            "WHERE n.type = 'ORGANIZATION_JOIN_REQUEST' AND n.target_id = #{orgId} AND n.sender_id IS NULL",
            "<if test='status != null'> AND n.status = #{status} </if>",
            "</script>"
    })
    long countJoinRequestsByOrg(@Param("orgId") Long orgId, @Param("status") String status);

    // ======= Bulk updates for organization teardown =======
    @Update("UPDATE notification SET status = 'EXPIRED', is_read = 1 WHERE target_id = #{orgId} AND type = 'ORGANIZATION_INVITE' AND status = 'ACTIVE'")
    int expireActiveInvitesByOrg(@Param("orgId") Long orgId);

    @Update("UPDATE notification SET status = 'EXPIRED', is_read = 1 WHERE target_id = #{orgId} AND type = 'ORGANIZATION_JOIN_REQUEST' AND status = 'ACTIVE'")
    int expireActiveJoinRequestsByOrg(@Param("orgId") Long orgId);
}
