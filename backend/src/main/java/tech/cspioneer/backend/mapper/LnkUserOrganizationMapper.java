package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.LnkUserOrganization;
import tech.cspioneer.backend.enums.OrganizationRole;

@Mapper
public interface LnkUserOrganizationMapper {

    @Select("SELECT id, uuid, organization_id AS organizationId, user_id AS userId, role FROM lnk_user_organization WHERE organization_id = #{orgId} AND user_id = #{userId} LIMIT 1")
    LnkUserOrganization findByOrgIdAndUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Insert("INSERT INTO lnk_user_organization(uuid, organization_id, user_id, role) VALUES(#{uuid}, #{organizationId}, #{userId}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LnkUserOrganization link);

    @Update("UPDATE lnk_user_organization SET role = #{role} WHERE organization_id = #{organizationId} AND user_id = #{userId}")
    int updateRole(@Param("organizationId") Long organizationId, @Param("userId") Long userId, @Param("role") OrganizationRole role);

    @Delete("DELETE FROM lnk_user_organization WHERE organization_id = #{organizationId} AND user_id = #{userId}")
    int delete(@Param("organizationId") Long organizationId, @Param("userId") Long userId);

    @Select("SELECT user_id FROM lnk_user_organization WHERE organization_id = #{orgId} AND role = 'ADMIN'")
    java.util.List<Long> listAdminUserIdsByOrgId(@Param("orgId") Long orgId);
}
