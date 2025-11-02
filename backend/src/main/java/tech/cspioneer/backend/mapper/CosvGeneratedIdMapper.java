package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import tech.cspioneer.backend.entity.CosvGeneratedId;

@Mapper
public interface CosvGeneratedIdMapper {
    @Insert("INSERT INTO cosv_generated_id() VALUES ()")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CosvGeneratedId id);
}

