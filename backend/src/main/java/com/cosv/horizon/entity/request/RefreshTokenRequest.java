package com.cosv.horizon.entity.request;

import lombok.Data;

/**
 * 刷新令牌请求实体类
 */
@Data
public class RefreshTokenRequest {
    /**
     * 刷新令牌
     */
    private String refreshToken;
} 