package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT id, uuid, code, name, description, create_date AS createDate FROM category WHERE uuid = #{uuid} LIMIT 1")
    Category findByUuid(@Param("uuid") String uuid);

    @Select("SELECT id, uuid, code, name, description, create_date AS createDate FROM category WHERE code = #{code} LIMIT 1")
    Category findByCode(@Param("code") String code);

    @Select("SELECT id, uuid, code, name, description, create_date AS createDate FROM category WHERE id = #{id} LIMIT 1")
    Category findById(@Param("id") Long id);

    @Insert("INSERT INTO category(uuid, code, name, description) VALUES(#{uuid}, #{code}, #{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category c);

    @Update({
            "<script>",
            "UPDATE category",
            "<set>",
            "<if test='code != null'> code = #{code}, </if>",
            "<if test='name != null'> name = #{name}, </if>",
            "<if test='description != null'> description = #{description}, </if>",
            "create_date = create_date",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateByUuid(Category c);

    @Delete("DELETE FROM category WHERE uuid = #{uuid}")
    int deleteByUuid(@Param("uuid") String uuid);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT id, uuid, code, name, description, create_date AS createDate",
            "FROM category",
            "<where>",
            "<if test='q != null'> (code LIKE CONCAT('%',#{q},'%') OR name LIKE CONCAT('%',#{q},'%')) </if>",
            "</where>",
            "ORDER BY name ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<Category> list(@Param("q") String q,
                        @Param("limit") int limit,
                        @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM category",
            "<where>",
            "<if test='q != null'> (code LIKE CONCAT('%',#{q},'%') OR name LIKE CONCAT('%',#{q},'%')) </if>",
            "</where>",
            "</script>"
    })
    long count(@Param("q") String q);
}
