package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.RawCosvFile;

@Mapper
public interface RawCosvFileMapper {

    @Insert("INSERT INTO raw_cosv_file(uuid, file_name, user_id, organization_id, status, status_message, content_length, create_date, update_date, storage_url, content, checksum_sha256, mime_type) " +
            "VALUES(#{uuid}, #{fileName}, #{userId}, #{organizationId}, #{status}, #{statusMessage}, #{contentLength}, NOW(), NOW(), #{storageUrl}, #{content}, #{checksumSha256}, #{mimeType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RawCosvFile f);

    @Select("SELECT id, uuid, file_name AS fileName, user_id AS userId, organization_id AS organizationId, status, status_message AS statusMessage, content_length AS contentLength, create_date AS createDate, update_date AS updateDate, storage_url AS storageUrl, content, checksum_sha256 AS checksumSha256, mime_type AS mimeType FROM raw_cosv_file WHERE uuid = #{uuid} LIMIT 1")
    RawCosvFile findByUuid(@Param("uuid") String uuid);

    @Update("UPDATE raw_cosv_file SET status = #{status}, status_message = #{statusMessage}, update_date = NOW() WHERE uuid = #{uuid}")
    int updateStatus(@Param("uuid") String uuid, @Param("status") String status, @Param("statusMessage") String statusMessage);
}

