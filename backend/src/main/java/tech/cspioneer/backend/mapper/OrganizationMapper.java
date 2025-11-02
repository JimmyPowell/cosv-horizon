package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Organization;
import tech.cspioneer.backend.dto.OrgWithRole;
import tech.cspioneer.backend.dto.OrgMemberView;

import java.util.List;

@Mapper
public interface OrganizationMapper {

    @Select("SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, free_text AS freeText, is_verified AS isVerified, reject_reason AS rejectReason, review_date AS reviewDate, reviewed_by AS reviewedBy, is_public AS isPublic, allow_join_request AS allowJoinRequest, allow_invite_link AS allowInviteLink FROM organization WHERE uuid = #{uuid} LIMIT 1")
    Organization findByUuid(@Param("uuid") String uuid);

    @Select("SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, free_text AS freeText, is_verified AS isVerified, reject_reason AS rejectReason, review_date AS reviewDate, reviewed_by AS reviewedBy, is_public AS isPublic, allow_join_request AS allowJoinRequest, allow_invite_link AS allowInviteLink FROM organization WHERE id = #{id} LIMIT 1")
    Organization findById(@Param("id") Long id);

    @Select("SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, free_text AS freeText, is_verified AS isVerified, reject_reason AS rejectReason, review_date AS reviewDate, reviewed_by AS reviewedBy, is_public AS isPublic, allow_join_request AS allowJoinRequest, allow_invite_link AS allowInviteLink FROM organization WHERE name = #{name} LIMIT 1")
    Organization findByName(@Param("name") String name);

    @Insert("INSERT INTO organization(uuid, name, status, avatar, description, free_text, is_verified, date_created, rating, is_public, allow_join_request, allow_invite_link) VALUES(#{uuid}, #{name}, #{status}, #{avatar}, #{description}, #{freeText}, COALESCE(#{isVerified}, 0), NOW(), COALESCE(#{rating},0), COALESCE(#{isPublic},1), COALESCE(#{allowJoinRequest},0), COALESCE(#{allowInviteLink},1))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Organization org);

    @Update({
            "<script>",
            "UPDATE organization",
            "<set>",
            "<if test='name != null'> name = #{name}, </if>",
            "<if test='avatar != null'> avatar = #{avatar}, </if>",
            "<if test='description != null'> description = #{description}, </if>",
            "<if test='freeText != null'> free_text = #{freeText}, </if>",
            "<if test='isVerified != null'> is_verified = #{isVerified}, </if>",
            "<if test='isPublic != null'> is_public = #{isPublic}, </if>",
            "<if test='allowJoinRequest != null'> allow_join_request = #{allowJoinRequest}, </if>",
            "<if test='allowInviteLink != null'> allow_invite_link = #{allowInviteLink}, </if>",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateBasic(Organization org);

    @Select("SELECT o.id AS id, o.uuid AS uuid, o.name AS name, o.status AS status, o.date_created AS dateCreated, o.avatar AS avatar, o.description AS description, o.rating AS rating, o.is_public AS is_public, o.allow_join_request AS allow_join_request, o.allow_invite_link AS allow_invite_link, luo.role AS role FROM organization o JOIN lnk_user_organization luo ON o.id = luo.organization_id WHERE luo.user_id = #{userId} ORDER BY o.date_created DESC")
    @Results(id="OrgWithRoleMap", value = {
            @Result(column = "id", property = "organization.id"),
            @Result(column = "uuid", property = "organization.uuid"),
            @Result(column = "name", property = "organization.name"),
            @Result(column = "status", property = "organization.status"),
            @Result(column = "dateCreated", property = "organization.dateCreated"),
            @Result(column = "avatar", property = "organization.avatar"),
            @Result(column = "description", property = "organization.description"),
            @Result(column = "rating", property = "organization.rating"),
            @Result(column = "is_public", property = "organization.isPublic"),
            @Result(column = "allow_join_request", property = "organization.allowJoinRequest"),
            @Result(column = "allow_invite_link", property = "organization.allowInviteLink"),
            @Result(column = "role", property = "role")
    })
    List<OrgWithRole> listByUserId(@Param("userId") Long userId);

    // Public search for organizations (for logged-in users)
    @Select({
            "<script>",
            "SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, is_public AS isPublic, allow_join_request AS allowJoinRequest, allow_invite_link AS allowInviteLink",
            "FROM organization",
            "WHERE is_public = 1 AND status = 'ACTIVE'",
            "<if test='q != null and q != \"\"'> AND (name LIKE CONCAT('%', #{q}, '%') OR uuid LIKE CONCAT('%', #{q}, '%')) </if>",
            "ORDER BY rating DESC, date_created DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<Organization> listPublicSearch(@Param("q") String q,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM organization",
            "WHERE is_public = 1 AND status = 'ACTIVE'",
            "<if test='q != null and q != \"\"'> AND (name LIKE CONCAT('%', #{q}, '%') OR uuid LIKE CONCAT('%', #{q}, '%')) </if>",
            "</script>"
    })
    long countPublicSearch(@Param("q") String q);

    @Select("SELECT u.uuid AS uuid, u.name AS name, u.email AS email, u.avatar AS avatar, luo.role AS role FROM lnk_user_organization luo JOIN `user` u ON u.id = luo.user_id WHERE luo.organization_id = #{orgId} ORDER BY u.name ASC")
    @Results(id="OrgMemberViewMap", value = {
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "email", property = "email"),
            @Result(column = "avatar", property = "avatar"),
            @Result(column = "role", property = "role")
    })
    List<OrgMemberView> listMembers(@Param("orgId") Long orgId);

    @Select("SELECT u.uuid AS uuid, u.name AS name, u.avatar AS avatar, luo.role AS role FROM lnk_user_organization luo JOIN `user` u ON u.id = luo.user_id WHERE luo.organization_id = #{orgId} ORDER BY u.name ASC")
    @Results(id="PublicOrgMemberViewMap", value = {
            @Result(column = "uuid", property = "uuid"),
            @Result(column = "name", property = "name"),
            @Result(column = "avatar", property = "avatar"),
            @Result(column = "role", property = "role")
    })
    java.util.List<tech.cspioneer.backend.dto.PublicOrgMemberView> listPublicMembers(@Param("orgId") Long orgId);

    @Update("UPDATE organization SET rating = rating + #{delta} WHERE id = #{id}")
    int incrementRating(@Param("id") Long id, @Param("delta") int delta);

    // ========== Admin listing & updates ==========
    @Select({
            "<script>",
            "SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, free_text AS freeText, reject_reason AS rejectReason, review_date AS reviewDate, reviewed_by AS reviewedBy",
            "FROM organization",
            "WHERE 1=1",
            "<if test='q != null'> AND (name LIKE CONCAT('%', #{q}, '%') OR uuid LIKE CONCAT('%', #{q}, '%')) </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "ORDER BY date_created DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<Organization> listAdmin(@Param("q") String q,
                                           @Param("status") String status,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM organization",
            "WHERE 1=1",
            "<if test='q != null'> AND (name LIKE CONCAT('%', #{q}, '%') OR uuid LIKE CONCAT('%', #{q}, '%')) </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "</script>"
    })
    long countAdmin(@Param("q") String q,
                    @Param("status") String status);

    @Update({
            "<script>",
            "UPDATE organization",
            "<set>",
            "  <if test='status != null'> status = #{status},</if>",
            "  <if test='rejectReasonSet'> reject_reason = #{rejectReason},</if>",
            "  <if test='reviewDate != null'> review_date = #{reviewDate},</if>",
            "  <if test='reviewedBy != null'> reviewed_by = #{reviewedBy},</if>",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateStatusReviewByUuid(@Param("uuid") String uuid,
                                 @Param("status") String status,
                                 @Param("rejectReason") String rejectReason,
                                 @Param("rejectReasonSet") boolean rejectReasonSet,
                                 @Param("reviewDate") java.time.LocalDateTime reviewDate,
                                 @Param("reviewedBy") Long reviewedBy);

    @Update({
            "<script>",
            "UPDATE organization",
            "<set>",
            "  <if test='status != null'> status = #{status},</if>",
            "  <if test='allowInviteLink != null'> allow_invite_link = #{allowInviteLink},</if>",
            "  <if test='allowJoinRequest != null'> allow_join_request = #{allowJoinRequest},</if>",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateStatusAndPoliciesByUuid(@Param("uuid") String uuid,
                                      @Param("status") String status,
                                      @Param("allowInviteLink") Boolean allowInviteLink,
                                      @Param("allowJoinRequest") Boolean allowJoinRequest);

    @Update({
            "<script>",
            "UPDATE organization",
            "<set>",
            "  <if test='name != null'> name = #{name},</if>",
            "  <if test='status != null'> status = #{status},</if>",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateNameAndStatusByUuid(@Param("uuid") String uuid,
                                  @Param("name") String name,
                                  @Param("status") String status);

    // ===== Leaderboard & ranking =====
    @Select({
            "<script>",
            "SELECT id, uuid, name, status, date_created AS dateCreated, avatar, description, rating, free_text AS freeText, is_verified AS isVerified, reject_reason AS rejectReason, review_date AS reviewDate, reviewed_by AS reviewedBy, is_public AS isPublic, allow_join_request AS allowJoinRequest, allow_invite_link AS allowInviteLink",
            "FROM organization",
            "WHERE status = 'ACTIVE'",
            "ORDER BY rating DESC, id ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<Organization> listByRating(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(1) FROM organization WHERE status = 'ACTIVE'")
    long countAllActive();

    @Select("SELECT COUNT(1) + 1 FROM organization WHERE rating > #{rating} AND status = 'ACTIVE'")
    long rankByRating(@Param("rating") long rating);
}
