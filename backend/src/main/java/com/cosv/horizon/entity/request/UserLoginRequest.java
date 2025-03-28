package com.cosv.horizon.entity.request;

import lombok.Data;

/**
 * 用户登录请求实体类
 */
@Data
public class UserLoginRequest {
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 用户密码
     */
    private String password;
} 