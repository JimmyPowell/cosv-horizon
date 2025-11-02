package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.OriginalLogin;

@Mapper
public interface OriginalLoginMapper {

    @Select("SELECT id, uuid, user_id AS userId, source, name FROM original_login WHERE source = #{source} AND name = #{name} LIMIT 1")
    OriginalLogin findBySourceAndName(@Param("source") String source, @Param("name") String name);

    @Select("SELECT id, uuid, user_id AS userId, source, name FROM original_login WHERE user_id = #{userId} AND source = #{source} LIMIT 1")
    OriginalLogin findByUserIdAndSource(@Param("userId") Long userId, @Param("source") String source);

    @Insert("INSERT INTO original_login(uuid, user_id, source, name) VALUES(#{uuid}, #{userId}, #{source}, #{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OriginalLogin record);

    @Delete("DELETE FROM original_login WHERE user_id = #{userId} AND source = #{source}")
    int deleteByUserIdAndSource(@Param("userId") Long userId, @Param("source") String source);
}

