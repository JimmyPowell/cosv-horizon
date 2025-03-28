package com.cosv.horizon.service;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.enums.UserStatus;
import com.cosv.horizon.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class GithubOauthService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OriginalLoginService originalLoginService;

    /**
     * 处理GitHub OAuth登录
     * @param authentication OAuth认证信息
     * @return 处理结果（用户信息）
     */
    @Transactional
    public User processOAuthPostLogin(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalArgumentException("非法的认证类型");
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // 应为"github"
        String providerId = attributes.get("id").toString();
        String login = attributes.get("login").toString();
        
        // 检查是否已有关联此GitHub账号的登录
        User user = originalLoginService.findUserByProviderAndProviderId(provider, providerId);
        
        // 如果没有关联账号，则创建新用户
        if (user == null) {
            user = createUserFromGithub(attributes);
            userMapper.insert(user);
            
            // 保存GitHub登录信息
            originalLoginService.saveOriginalLogin(user.getId(), provider, providerId, login);
        }
        
        return user;
    }
    
    /**
     * 从GitHub属性创建用户
     */
    private User createUserFromGithub(Map<String, Object> attributes) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setName((String) attributes.get("login"));
        user.setEmail((String) attributes.get("email"));
        user.setAvatar((String) attributes.get("avatar_url"));
        user.setCompany((String) attributes.get("company"));
        user.setLocation((String) attributes.get("location"));
        user.setGitHub((String) attributes.get("html_url"));
        user.setWebsite((String) attributes.get("blog"));
        user.setFreeText((String) attributes.get("bio"));
        user.setStatus(UserStatus.NORMAL);
        user.setRole("USER");
        user.setRating(0L);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        
        return user;
    }
} 