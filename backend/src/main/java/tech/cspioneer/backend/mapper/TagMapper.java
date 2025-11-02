package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Tag;

@Mapper
public interface TagMapper {

    @Select("SELECT id, uuid, code, name, create_date AS createDate FROM tag WHERE name = #{name} LIMIT 1")
    Tag findByName(@Param("name") String name);

    @Select("SELECT id, uuid, code, name, create_date AS createDate FROM tag WHERE code = #{code} LIMIT 1")
    Tag findByCode(@Param("code") String code);

    @Select("SELECT id, uuid, code, name, create_date AS createDate FROM tag WHERE uuid = #{uuid} LIMIT 1")
    Tag findByUuid(@Param("uuid") String uuid);

    @Insert("INSERT INTO tag(uuid, code, name, create_date) VALUES(#{uuid}, #{code}, #{name}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Select({
            "<script>",
            "SELECT id, uuid, code, name, create_date AS createDate FROM tag",
            "<if test='q != null'> WHERE name LIKE CONCAT('%',#{q},'%') </if>",
            "ORDER BY name ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<Tag> list(@Param("q") String q,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM tag",
            "<if test='q != null'> WHERE name LIKE CONCAT('%',#{q},'%') </if>",
            "</script>"
    })
    long count(@Param("q") String q);

    @Select("SELECT COUNT(1) FROM tag WHERE name = #{name}")
    long countByName(@Param("name") String name);

    @Select("SELECT COUNT(1) FROM tag WHERE code = #{code}")
    long countByCode(@Param("code") String code);

    @Select("SELECT t.id, t.uuid, t.code, t.name, t.create_date AS createDate FROM tag t JOIN lnk_vulnerability_metadata_tag l ON l.tag_id = t.id WHERE l.vulnerability_metadata_id = #{vmId}")
    java.util.List<Tag> listByVulnerabilityId(@Param("vmId") Long vmId);

    @Delete("DELETE FROM tag WHERE uuid = #{uuid}")
    int deleteByUuid(@Param("uuid") String uuid);

    @Delete("DELETE FROM tag WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update({
            "<script>",
            "UPDATE tag",
            "<set>",
            "<if test='code != null'> code = #{code}, </if>",
            "<if test='name != null'> name = #{name}, </if>",
            "create_date = create_date",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateByUuid(tech.cspioneer.backend.entity.Tag tag);
}
