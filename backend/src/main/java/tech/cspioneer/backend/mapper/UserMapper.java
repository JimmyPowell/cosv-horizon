package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate FROM `user` WHERE email = #{email} LIMIT 1")
    User findByEmail(@Param("email") String email);

    @Select("SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate FROM `user` WHERE name = #{name} LIMIT 1")
    User findByName(@Param("name") String name);

    @Select("SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate FROM `user` WHERE email = #{login} OR name = #{login} LIMIT 1")
    User findByEmailOrName(@Param("login") String login);

    @Select("SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate FROM `user` WHERE uuid = #{uuid} LIMIT 1")
    User findByUuid(@Param("uuid") String uuid);

    @Select("SELECT uuid FROM `user` WHERE id = #{id} LIMIT 1")
    String findUuidById(@Param("id") Long id);

    @Update("UPDATE `user` SET rating = rating + #{delta} WHERE id = #{id}")
    int incrementRating(@Param("id") Long id, @Param("delta") int delta);

    @Insert("INSERT INTO `user`(uuid, name, password, role, email, real_name, company, location, status, create_date, update_date) VALUES (#{uuid}, #{name}, #{password}, #{role}, #{email}, #{realName}, #{company}, #{location}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET password = #{password}, update_date = NOW() WHERE email = #{email} LIMIT 1")
    int updatePasswordByEmail(@Param("email") String email, @Param("password") String password);
    
    @Update("UPDATE `user` SET password = #{password}, update_date = NOW() WHERE uuid = #{uuid} LIMIT 1")
    int updatePasswordByUuid(@Param("uuid") String uuid, @Param("password") String password);

    @Update({
            "<script>",
            "UPDATE `user`",
            "<set>",
            "  <if test='avatar != null'>avatar = #{avatar},</if>",
            "  <if test='company != null'>company = #{company},</if>",
            "  <if test='location != null'>location = #{location},</if>",
            "  <if test='gitHub != null'>git_hub = #{gitHub},</if>",
            "  <if test='website != null'>website = #{website},</if>",
            "  <if test='freeText != null'>free_text = #{freeText},</if>",
            "  <if test='realName != null'>real_name = #{realName},</if>",
            "  update_date = NOW()",
            "</set>",
            "WHERE uuid = #{uuid}",
            "</script>"
    })
    int updateProfileByUuid(@Param("uuid") String uuid,
                            @Param("avatar") String avatar,
                            @Param("company") String company,
                            @Param("location") String location,
                            @Param("gitHub") String gitHub,
                            @Param("website") String website,
                            @Param("freeText") String freeText,
                            @Param("realName") String realName);

    @Update("UPDATE `user` SET name = #{name}, update_date = NOW() WHERE uuid = #{uuid} LIMIT 1")
    int updateNameByUuid(@Param("uuid") String uuid, @Param("name") String name);

    @Update("UPDATE `user` SET email = #{email}, update_date = NOW() WHERE uuid = #{uuid} LIMIT 1")
    int updateEmailByUuid(@Param("uuid") String uuid, @Param("email") String email);

    // ========== Admin listing & updates ==========
    @Select({
            "<script>",
            "SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate",
            "FROM `user`",
            "WHERE 1=1",
            "<if test='q != null'> AND (name LIKE CONCAT('%', #{q}, '%') OR email LIKE CONCAT('%', #{q}, '%')) </if>",
            "<if test='role != null'> AND role = #{role} </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "ORDER BY create_date DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<User> listAdmin(@Param("q") String q,
                                   @Param("role") String role,
                                   @Param("status") String status,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM `user`",
            "WHERE 1=1",
            "<if test='q != null'> AND (name LIKE CONCAT('%', #{q}, '%') OR email LIKE CONCAT('%', #{q}, '%')) </if>",
            "<if test='role != null'> AND role = #{role} </if>",
            "<if test='status != null'> AND status = #{status} </if>",
            "</script>"
    })
    long countAdmin(@Param("q") String q,
                    @Param("role") String role,
                    @Param("status") String status);

    @Update("UPDATE `user` SET role = #{role}, update_date = NOW() WHERE uuid = #{uuid} LIMIT 1")
    int updateRoleByUuid(@Param("uuid") String uuid, @Param("role") String role);

    @Update("UPDATE `user` SET status = #{status}, update_date = NOW() WHERE uuid = #{uuid} LIMIT 1")
    int updateStatusByUuid(@Param("uuid") String uuid, @Param("status") String status);

    // ===== Leaderboard & ranking =====
    @Select({
            "<script>",
            "SELECT id, uuid, name, password, role, email, avatar, company, location, git_hub AS gitHub, status, rating, website, free_text AS freeText, real_name AS realName, create_date AS createDate, update_date AS updateDate",
            "FROM `user`",
            "ORDER BY rating DESC, id ASC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    java.util.List<User> listByRating(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(1) FROM `user`")
    long countAll();

    @Select("SELECT COUNT(1) + 1 FROM `user` WHERE rating > #{rating}")
    long rankByRating(@Param("rating") long rating);
}
