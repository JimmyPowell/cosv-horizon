package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.OrgPointsLedger;

import java.util.List;

@Mapper
public interface OrgPointsLedgerMapper {

    @Insert("INSERT INTO org_points_ledger(uuid, organization_id, delta, reason, ref_type, ref_id, idempotency_key, created_at) VALUES(#{uuid}, #{organizationId}, #{delta}, #{reason}, #{refType}, #{refId}, #{idempotencyKey}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrgPointsLedger l);

    @Select("SELECT 1 FROM org_points_ledger WHERE organization_id = #{orgId} AND idempotency_key = #{idem} LIMIT 1")
    Integer existsByIdem(@Param("orgId") Long orgId, @Param("idem") String idem);

    @Select({
            "<script>",
            "SELECT id, uuid, organization_id AS organizationId, delta, reason, ref_type AS refType, ref_id AS refId, idempotency_key AS idempotencyKey, created_at AS createdAt",
            "FROM org_points_ledger",
            "WHERE organization_id = #{orgId}",
            "ORDER BY created_at DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<OrgPointsLedger> listByOrg(@Param("orgId") Long orgId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(1) FROM org_points_ledger WHERE organization_id = #{orgId}")
    long countByOrg(@Param("orgId") Long orgId);
}

