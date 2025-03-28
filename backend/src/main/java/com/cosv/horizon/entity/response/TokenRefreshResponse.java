package com.cosv.horizon.entity.response;

import lombok.Data;

/**
 * 令牌刷新响应实体类
 */
@Data
public class TokenRefreshResponse {
    /**
     * 新的访问令牌
     */
    private String accessToken;
    
    /**
     * 新的刷新令牌，如果原刷新令牌接近过期，则返回新的刷新令牌，否则为null
     */
    private String refreshToken;
    
    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
} 