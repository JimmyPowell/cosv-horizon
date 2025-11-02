package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.ApiKey;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApiKeyMapper {

    @Insert("INSERT INTO api_key(uuid, key_prefix, key_hash, creator_user_id, organization_id, description, scopes, status, expire_time, create_time, update_time) VALUES(#{uuid}, #{keyPrefix}, #{keyHash}, #{creatorUserId}, #{organizationId}, #{description}, #{scopes}, #{status}, #{expireTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiKey key);

    @Select("SELECT id, uuid, key_prefix AS keyPrefix, key_hash AS keyHash, creator_user_id AS creatorUserId, organization_id AS organizationId, description, scopes, status, last_used_time AS lastUsedTime, last_used_ip AS lastUsedIp, expire_time AS expireTime, create_time AS createTime, update_time AS updateTime FROM api_key WHERE uuid = #{uuid} LIMIT 1")
    ApiKey findByUuid(@Param("uuid") String uuid);

    @Select("SELECT id, uuid, key_prefix AS keyPrefix, key_hash AS keyHash, creator_user_id AS creatorUserId, organization_id AS organizationId, description, scopes, status, last_used_time AS lastUsedTime, last_used_ip AS lastUsedIp, expire_time AS expireTime, create_time AS createTime, update_time AS updateTime FROM api_key WHERE key_prefix = #{prefix} AND key_hash = #{hash} AND status = 'ACTIVE' AND (expire_time IS NULL OR expire_time > NOW()) LIMIT 1")
    ApiKey findActiveByPrefixAndHash(@Param("prefix") String prefix, @Param("hash") String hash);

    @Update("UPDATE api_key SET status = #{status}, update_time = NOW() WHERE uuid = #{uuid}")
    int updateStatus(@Param("uuid") String uuid, @Param("status") String status);

    @Update("UPDATE api_key SET last_used_time = NOW(), last_used_ip = #{ip} WHERE id = #{id}")
    int touch(@Param("id") Long id, @Param("ip") String ip);

    @Select("SELECT id, uuid, key_prefix AS keyPrefix, key_hash AS keyHash, creator_user_id AS creatorUserId, organization_id AS organizationId, description, scopes, status, last_used_time AS lastUsedTime, last_used_ip AS lastUsedIp, expire_time AS expireTime, create_time AS createTime, update_time AS updateTime FROM api_key WHERE creator_user_id = #{userId} ORDER BY create_time DESC")
    List<ApiKey> listByCreator(@Param("userId") Long userId);

    @Select("SELECT id, uuid, key_prefix AS keyPrefix, key_hash AS keyHash, creator_user_id AS creatorUserId, organization_id AS organizationId, description, scopes, status, last_used_time AS lastUsedTime, last_used_ip AS lastUsedIp, expire_time AS expireTime, create_time AS createTime, update_time AS updateTime FROM api_key WHERE organization_id = #{orgId} ORDER BY create_time DESC")
    List<ApiKey> listByOrganization(@Param("orgId") Long orgId);

    @Update({
            "<script>",
            "UPDATE api_key",
            "<set>",
            "  <if test='descriptionProvided'>description = #{description},</if>",
            "  <if test='scopesProvided'>scopes = #{scopes},</if>",
            "  <if test='expireTimeProvided'>expire_time = #{expireTime},</if>",
            "  update_time = NOW()",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateFields(@Param("uuid") String uuid,
                     @Param("description") String description,
                     @Param("descriptionProvided") boolean descriptionProvided,
                     @Param("scopes") String scopes,
                     @Param("scopesProvided") boolean scopesProvided,
                     @Param("expireTime") LocalDateTime expireTime,
                     @Param("expireTimeProvided") boolean expireTimeProvided);
}
