package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.AppSetting;

import java.util.List;

@Mapper
public interface AppSettingMapper {

    @Select("SELECT `key`, `value`, updated_at AS updatedAt FROM app_setting WHERE `key` = #{key} LIMIT 1")
    AppSetting get(@Param("key") String key);

    @Insert("INSERT INTO app_setting(`key`, `value`, updated_at) VALUES(#{key}, #{value}, NOW()) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`), updated_at = NOW()")
    int upsert(@Param("key") String key, @Param("value") String value);

    @Select({
            "<script>",
            "SELECT `key`, `value`, updated_at AS updatedAt FROM app_setting",
            "<if test='prefix != null'> WHERE `key` LIKE CONCAT(#{prefix}, '%') </if>",
            "ORDER BY `key` ASC",
            "</script>"
    })
    List<AppSetting> listByPrefix(@Param("prefix") String prefix);
}
