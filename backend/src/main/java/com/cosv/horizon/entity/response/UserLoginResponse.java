package com.cosv.horizon.entity.response;

import lombok.Data;

/**
 * 用户登录响应实体类
 */
@Data
public class UserLoginResponse {
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    public UserLoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
} 