package com.cosv.horizon.service;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.CodeVerifyRequest;
import com.cosv.horizon.entity.request.UserLoginRequest;
import com.cosv.horizon.entity.request.VerifySessionRequest;
import com.cosv.horizon.entity.response.TokenRefreshResponse;

public interface AuthService {

    /**
     * 生成验证码并发送到指定邮箱
     * @param email 目标邮箱
     * @return 是否发送成功
     */
    boolean generateCode(String email);
    
    /**
     * 验证验证码是否正确
     * @param request 包含邮箱和验证码的请求
     * @return 是否验证成功
     */
    boolean validateCode(CodeVerifyRequest request);
    
    /**
     * 发送验证码并创建会话
     * @param email 目标邮箱
     * @return 会话ID
     */
    String sendVerificationCode(String email);
    
    /**
     * 验证会话和验证码
     * @param request 会话验证请求
     * @return 验证结果
     */
    boolean verifySession(VerifySessionRequest request);
    
    /**
     * 用户登录
     * @param request 登录请求，包含用户名/邮箱和密码
     * @return 登录成功返回用户信息，失败返回null
     */
    User login(UserLoginRequest request);
    
    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 刷新成功返回新的令牌，失败返回null
     */
    TokenRefreshResponse refreshToken(String refreshToken);
    
    /**
     * 用户登出
     * @param userUuid 用户UUID
     * @return 是否成功
     */
    boolean logout(String userUuid);
}
