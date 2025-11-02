package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import tech.cspioneer.backend.entity.ApiKeyUsageLog;

@Mapper
public interface ApiKeyUsageLogMapper {
    @Insert("INSERT INTO api_key_usage_log(uuid, api_key_id, request_timestamp, request_ip_address, request_method, request_path, response_status_code, user_agent) VALUES(#{uuid}, #{apiKeyId}, NOW(), #{requestIpAddress}, #{requestMethod}, #{requestPath}, #{responseStatusCode}, #{userAgent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiKeyUsageLog log);

    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT id, uuid, api_key_id AS apiKeyId, request_timestamp AS requestTimestamp, request_ip_address AS requestIpAddress, request_method AS requestMethod, request_path AS requestPath, response_status_code AS responseStatusCode, user_agent AS userAgent",
            "FROM api_key_usage_log",
            "WHERE api_key_id = #{apiKeyId}",
            "<if test='fromTs != null'> AND request_timestamp &gt;= #{fromTs} </if>",
            "<if test='toTs != null'> AND request_timestamp &lt;= #{toTs} </if>",
            "ORDER BY request_timestamp DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<tech.cspioneer.backend.entity.ApiKeyUsageLog> listByApiKey(@org.apache.ibatis.annotations.Param("apiKeyId") Long apiKeyId,
                                                                              @org.apache.ibatis.annotations.Param("fromTs") String fromTs,
                                                                              @org.apache.ibatis.annotations.Param("toTs") String toTs,
                                                                              @org.apache.ibatis.annotations.Param("limit") int limit,
                                                                              @org.apache.ibatis.annotations.Param("offset") int offset);

    @org.apache.ibatis.annotations.Select({
            "<script>",
            "SELECT COUNT(1) FROM api_key_usage_log WHERE api_key_id = #{apiKeyId}",
            "<if test='fromTs != null'> AND request_timestamp &gt;= #{fromTs} </if>",
            "<if test='toTs != null'> AND request_timestamp &lt;= #{toTs} </if>",
            "</script>"
    })
    long countByApiKey(@org.apache.ibatis.annotations.Param("apiKeyId") Long apiKeyId,
                       @org.apache.ibatis.annotations.Param("fromTs") String fromTs,
                       @org.apache.ibatis.annotations.Param("toTs") String toTs);
}
