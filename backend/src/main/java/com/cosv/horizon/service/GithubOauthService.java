package com.cosv.horizon.service;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.dto.GithubUserDTO;
import com.cosv.horizon.entity.response.UserLoginResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * GitHub OAuth服务接口
 * 处理GitHub OAuth认证流程
 */
public interface GithubOAuthService {
    
    /**
     * 获取GitHub认证URL
     * @return GitHub认证URL
     */
    String getAuthorizationUrl();
    
    /**
     * 使用授权码获取GitHub访问令牌
     * @param code GitHub授权码
     * @return 访问令牌
     */
    String getAccessToken(String code);
    
    /**
     * 使用访问令牌获取GitHub用户信息
     * @param accessToken GitHub访问令牌
     * @return GitHub用户信息
     */
    GithubUserDTO getUserInfo(String accessToken);
    
    /**
     * 处理GitHub登录
     * @param githubUser GitHub用户信息
     * @return 用户登录响应，包含JWT令牌
     */
    UserLoginResponse handleLogin(GithubUserDTO githubUser);
} 