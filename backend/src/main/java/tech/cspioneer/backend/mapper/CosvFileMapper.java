package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.CosvFile;

@Mapper
public interface CosvFileMapper {
    @Insert("INSERT INTO cosv_file(uuid, identifier, modified, prev_cosv_file_id, user_id, schema_version, raw_cosv_file_id) VALUES(#{uuid}, #{identifier}, NOW(), #{prevCosvFileId}, #{userId}, #{schemaVersion}, #{rawCosvFileId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CosvFile file);
}
