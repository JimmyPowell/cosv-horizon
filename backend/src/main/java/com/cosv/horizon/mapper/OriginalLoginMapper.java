package com.cosv.horizon.mapper;

import com.cosv.horizon.entity.OriginalLogin;
import com.cosv.horizon.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OriginalLoginMapper {

    /**
     * 根据提供商和提供商ID查找用户
     * @param provider 提供商（如github）
     * @param providerId 提供商ID
     * @return 关联的用户，如不存在则返回null
     */
    @Select("SELECT u.* FROM user u " +
            "JOIN original_login ol ON u.id = ol.user_id " +
            "WHERE ol.source = #{provider} AND ol.name = #{providerId}")
    User findUserByProviderAndProviderId(@Param("provider") String provider, 
                                         @Param("providerId") String providerId);

    /**
     * 插入原始登录信息
     * @param originalLogin 原始登录信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO original_login (user_id, source, name) " +
            "VALUES (#{userId}, #{source}, #{name})")
    int insert(OriginalLogin originalLogin);
} 