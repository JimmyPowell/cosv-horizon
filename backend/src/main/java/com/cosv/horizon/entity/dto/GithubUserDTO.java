package com.cosv.horizon.entity.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * GitHub用户数据传输对象
 * 用于从GitHub API获取的用户信息
 */
@Data
public class GithubUserDTO implements Serializable {
    private Long id;              // GitHub用户ID
    private String login;         // GitHub登录名
    private String name;          // 用户全名
    private String email;         // 用户邮箱
    private String avatar_url;    // 用户头像URL
    private String location;      // 用户位置
    private String company;       // 用户公司
    private String bio;           // 用户简介
    private String blog;          // 用户博客/网站
    private Boolean email_verified; // 邮箱是否已验证
    
    // GitHub API不会直接返回这个字段，我们需要手动设置它
    private String accessToken;   // 访问令牌
} 