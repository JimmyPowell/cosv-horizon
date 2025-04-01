package com.cosv.horizon.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosv.horizon.entity.OriginalLogin;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.dto.GithubUserDTO;
import com.cosv.horizon.entity.response.UserLoginResponse;
import com.cosv.horizon.enums.UserStatus;
import com.cosv.horizon.mapper.OriginalLoginMapper;
import com.cosv.horizon.mapper.UserMapper;
import com.cosv.horizon.service.GithubOAuthService;
import com.cosv.horizon.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GitHub OAuth服务实现类
 */
@Service
@Slf4j
public class GithubOAuthServiceImpl implements GithubOAuthService {

    @Value("${github.client.id:Ov23lifwAxy6dSKVAv1X}")
    private String clientId;

    @Value("${github.client.secret:1e28e69657bc2a34fbd989eb08e0ba133322f479}")
    private String clientSecret;

    @Value("${github.redirect.uri:http://localhost:8082/login/oauth2/code/github}")
    private String redirectUri;
    
    @Value("${github.api.authorization-uri:https://github.com/login/oauth/authorize}")
    private String authorizationUri;

    @Value("${github.api.token-uri:https://github.com/login/oauth/access_token}")
    private String tokenUri;

    @Value("${github.api.user-info-uri:https://api.github.com/user}")
    private String userInfoUri;
    
    @Value("${github.scope:}")
    private String[] scopes;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OriginalLoginMapper originalLoginMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String getAuthorizationUrl() {
        StringBuilder urlBuilder = new StringBuilder(authorizationUri);
        urlBuilder.append("?client_id=").append(clientId);
        
        // 添加scope
        if (scopes != null && scopes.length > 0) {
            String scopeParam = String.join(",", scopes);
            urlBuilder.append("&scope=").append(scopeParam);
        } else {
            urlBuilder.append("&scope=user,user:email");
        }
        
        // 添加重定向URI
        if (redirectUri != null && !redirectUri.isEmpty()) {
            urlBuilder.append("&redirect_uri=").append(redirectUri);
        }
        
        // 添加state参数（可选，用于防止CSRF）
        String state = UUID.randomUUID().toString();
        urlBuilder.append("&state=").append(state);
        
        return urlBuilder.toString();
    }

    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        JSONObject requestJson = new JSONObject();
        requestJson.put("client_id", clientId);
        requestJson.put("client_secret", clientSecret);
        requestJson.put("code", code);
        if (redirectUri != null && !redirectUri.isEmpty()) {
            requestJson.put("redirect_uri", redirectUri);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(requestJson.toJSONString(), headers);
        
        try {
            log.debug("请求GitHub token，URL: {}, 请求体: {}", tokenUri, requestJson);
            // 修改为Map接收响应
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            // 从Map中提取access_token字符串
            Map<String, String> tokenResponse = response.getBody();
            if (tokenResponse != null) {
                log.debug("GitHub token响应: {}", tokenResponse.entrySet().stream()
                        .map(e -> e.getKey() + "=" + (e.getKey().contains("token") ? "***" : e.getValue()))
                        .collect(Collectors.joining(", ")));
                
                if (tokenResponse.containsKey("access_token")) {
                    log.info("获取GitHub访问令牌成功");
                    return tokenResponse.get("access_token");
                } else {
                    log.error("获取GitHub访问令牌失败：响应不包含access_token: {}", tokenResponse);
                }
            } else {
                log.error("获取GitHub访问令牌失败：响应为空");
            }
            return null;
        } catch (Exception e) {
            log.error("获取GitHub访问令牌异常", e);
            return null;
        }
    }

    @Override
    public GithubUserDTO getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        try {
            log.debug("请求GitHub用户信息，URL: {}", userInfoUri);
            // 获取用户基本信息
            ResponseEntity<JSONObject> userResponse = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    JSONObject.class
            );
            
            JSONObject userJson = userResponse.getBody();
            if (userJson == null) {
                log.error("获取GitHub用户信息失败：响应为空");
                return null;
            }
            
            log.debug("GitHub用户信息响应: {}", userJson);
            log.info("获取GitHub用户信息成功: {}", userJson.getString("login"));
            
            GithubUserDTO githubUser = JSONObject.parseObject(userJson.toJSONString(), GithubUserDTO.class);
            githubUser.setAccessToken(accessToken);
            
            // 如果邮箱为空，则获取用户邮箱信息
            if (githubUser.getEmail() == null || githubUser.getEmail().isEmpty()) {
                try {
                    log.debug("用户邮箱为空，请求GitHub邮箱API");
                    ResponseEntity<JSONArray> emailResponse = restTemplate.exchange(
                            userInfoUri + "/emails",
                            HttpMethod.GET,
                            entity,
                            JSONArray.class
                    );
                    
                    JSONArray emails = emailResponse.getBody();
                    if (emails != null) {
                        log.debug("GitHub邮箱API响应: {}", emails);
                        
                        // 找到主要邮箱
                        for (int i = 0; i < emails.size(); i++) {
                            JSONObject emailObj = emails.getJSONObject(i);
                            if (emailObj.getBooleanValue("primary")) {
                                githubUser.setEmail(emailObj.getString("email"));
                                githubUser.setEmail_verified(emailObj.getBooleanValue("verified"));
                                log.info("找到用户主要邮箱: {}, 已验证: {}", 
                                        githubUser.getEmail(), githubUser.getEmail_verified());
                                break;
                            }
                        }
                    } else {
                        log.warn("获取GitHub用户邮箱失败：响应为空");
                    }
                } catch (Exception e) {
                    log.error("获取GitHub用户邮箱失败", e);
                }
            }
            
            return githubUser;
        } catch (Exception e) {
            log.error("获取GitHub用户信息异常", e);
            return null;
        }
    }

    @Override
    @Transactional
    public UserLoginResponse handleLogin(GithubUserDTO githubUser) {
        // 查找是否已有该GitHub账号关联的用户
        User user = originalLoginMapper.findUserByProviderAndProviderId("github", String.valueOf(githubUser.getId()));
        
        if (user == null) {
            // 创建新用户
            user = createUserFromGithub(githubUser);
            
            // 关联GitHub账号
            OriginalLogin originalLogin = new OriginalLogin(
                    user.getId(),
                    "github",
                    String.valueOf(githubUser.getId())
            );
            originalLoginMapper.insert(originalLogin);
            
            log.info("创建新用户并关联GitHub账号: {}", user.getName());
        } else {
            // 更新用户信息
            updateUserFromGithub(user, githubUser);
            log.info("更新已有用户信息: {}", user.getName());
        }
        
        // 生成JWT令牌
        Map<String, String> tokens = jwtUtil.generateToken(user);
        
        // 创建登录响应
        UserLoginResponse response = new UserLoginResponse(
            tokens.get("accessToken"), 
            tokens.get("refreshToken")
        );
        
        return response;
    }
    
    /**
     * 从GitHub用户信息创建新用户
     * @param githubUser GitHub用户信息
     * @return 新创建的用户
     */
    private User createUserFromGithub(GithubUserDTO githubUser) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        
        // 使用GitHub登录名作为用户名，如果已存在则添加随机后缀
        String username = githubUser.getLogin();
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            username = username + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        user.setName(username);
        user.setEmail(githubUser.getEmail());
        user.setAvatar(githubUser.getAvatar_url());
        user.setGitHub("https://github.com/" + githubUser.getLogin());
        user.setCompany(githubUser.getCompany());
        user.setLocation(githubUser.getLocation());
        user.setWebsite(githubUser.getBlog());
        user.setFreeText(githubUser.getBio());
        user.setRole("USER");
        user.setStatus(UserStatus.NORMAL);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 从GitHub用户信息更新已有用户
     * @param user 已有用户
     * @param githubUser GitHub用户信息
     */
    private void updateUserFromGithub(User user, GithubUserDTO githubUser) {
        boolean updated = false;
        
        if (githubUser.getEmail() != null && !githubUser.getEmail().equals(user.getEmail())) {
            user.setEmail(githubUser.getEmail());
            updated = true;
        }
        
        if (githubUser.getAvatar_url() != null && !githubUser.getAvatar_url().equals(user.getAvatar())) {
            user.setAvatar(githubUser.getAvatar_url());
            updated = true;
        }
        
        if (githubUser.getCompany() != null && !githubUser.getCompany().equals(user.getCompany())) {
            user.setCompany(githubUser.getCompany());
            updated = true;
        }
        
        if (githubUser.getLocation() != null && !githubUser.getLocation().equals(user.getLocation())) {
            user.setLocation(githubUser.getLocation());
            updated = true;
        }
        
        if (githubUser.getBlog() != null && !githubUser.getBlog().equals(user.getWebsite())) {
            user.setWebsite(githubUser.getBlog());
            updated = true;
        }
        
        if (githubUser.getBio() != null && !githubUser.getBio().equals(user.getFreeText())) {
            user.setFreeText(githubUser.getBio());
            updated = true;
        }
        
        if (updated) {
            user.setUpdateDate(new Date());
            userMapper.updateUserInfo(user);
        }
    }
} 