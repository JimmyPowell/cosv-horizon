package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.OrgInviteLink;

import java.util.List;

@Mapper
public interface OrgInviteLinkMapper {

    @Insert("INSERT INTO org_invite_link(uuid, org_id, code, created_by, create_time, expire_time, is_active) VALUES(#{uuid}, #{orgId}, #{code}, #{createdBy}, NOW(), #{expireTime}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrgInviteLink link);

    @Select("SELECT id, uuid, org_id AS orgId, code, created_by AS createdBy, create_time AS createTime, expire_time AS expireTime, is_active AS isActive FROM org_invite_link WHERE uuid = #{uuid} LIMIT 1")
    OrgInviteLink findByUuid(@Param("uuid") String uuid);

    @Select("SELECT id, uuid, org_id AS orgId, code, created_by AS createdBy, create_time AS createTime, expire_time AS expireTime, is_active AS isActive FROM org_invite_link WHERE code = #{code} LIMIT 1")
    OrgInviteLink findByCode(@Param("code") String code);

    @Select("SELECT id, uuid, org_id AS orgId, code, created_by AS createdBy, create_time AS createTime, expire_time AS expireTime, is_active AS isActive FROM org_invite_link WHERE org_id = #{orgId} ORDER BY create_time DESC")
    List<OrgInviteLink> listByOrgId(@Param("orgId") Long orgId);

    @Update("UPDATE org_invite_link SET is_active = 0 WHERE uuid = #{uuid}")
    int deactivateByUuid(@Param("uuid") String uuid);

    @Update("UPDATE org_invite_link SET is_active = 0 WHERE org_id = #{orgId}")
    int deactivateAllByOrgId(@Param("orgId") Long orgId);
}
