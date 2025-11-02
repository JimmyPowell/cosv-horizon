package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.UserPointsLedger;

import java.util.List;

@Mapper
public interface UserPointsLedgerMapper {

    @Insert("INSERT INTO user_points_ledger(uuid, user_id, delta, reason, ref_type, ref_id, idempotency_key, created_at) VALUES(#{uuid}, #{userId}, #{delta}, #{reason}, #{refType}, #{refId}, #{idempotencyKey}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPointsLedger l);

    @Select("SELECT 1 FROM user_points_ledger WHERE user_id = #{userId} AND idempotency_key = #{idem} LIMIT 1")
    Integer existsByIdem(@Param("userId") Long userId, @Param("idem") String idem);

    @Select({
            "<script>",
            "SELECT id, uuid, user_id AS userId, delta, reason, ref_type AS refType, ref_id AS refId, idempotency_key AS idempotencyKey, created_at AS createdAt",
            "FROM user_points_ledger",
            "WHERE user_id = #{userId}",
            "ORDER BY created_at DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<UserPointsLedger> listByUser(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(1) FROM user_points_ledger WHERE user_id = #{userId}")
    long countByUser(@Param("userId") Long userId);
}

