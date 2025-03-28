package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface UserMapper {
    /**
     * 插入新用户
     * @param user 用户信息
     * @return 影响的行数
     */
    int insert(User user);
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE name = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE email = #{email} LIMIT 1")
    User findByEmail(@Param("email") String email);
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE id = #{id} LIMIT 1")
    User findById(@Param("id") Long id);
    
    /**
     * 根据UUID查找用户
     * @param uuid 用户UUID
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE uuid = #{uuid} LIMIT 1")
    User findByUuid(@Param("uuid") String uuid);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码（已加密）
     * @param updateDate 更新时间
     * @return 影响的行数
     */
    int updatePassword(@Param("userId") Long userId, 
                       @Param("password") String password, 
                       @Param("updateDate") Date updateDate);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响的行数
     */
    int updateUserInfo(User user);
}
