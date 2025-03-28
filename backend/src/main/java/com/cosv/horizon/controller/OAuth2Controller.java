package com.cosv.horizon.controller;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.service.GithubOauthService;
import com.cosv.horizon.utils.Constans;
import com.cosv.horizon.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * GitHub OAuth2认证控制器
 * 处理GitHub OAuth2授权流程相关的接口
 */
@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    @Autowired
    private GithubOauthService githubOauthService;
   
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * GitHub OAuth2登录成功处理
     * 生成JWT令牌并返回用户信息
     * @return 包含用户信息和JWT令牌的响应
     */
    @GetMapping("/success")
    public ResponseEntity<HttpResponseEntity> loginSuccess() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = githubOauthService.processOAuthPostLogin(authentication);
            
            Map<String, String> tokens = jwtUtil.generateToken(user);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", tokens.get("accessToken"));
            responseData.put("refreshToken", tokens.get("refreshToken"));
            responseData.put("user", user);
            
            logger.info("GitHub OAuth2登录成功，用户: {}", user.getName());
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                responseData,
                "GitHub登录成功"
            ));
        } catch (Exception e) {
            logger.error("GitHub OAuth2登录失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "GitHub登录失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GitHub OAuth2登录失败处理
     * @return 登录失败的响应
     */
    @GetMapping("/failure")
    public ResponseEntity<HttpResponseEntity> loginFailure() {
        logger.warn("GitHub OAuth2登录失败");
        return ResponseEntity.ok(new HttpResponseEntity(
            Constans.AUTH_ERROR_CODE,
            null,
            "GitHub登录失败"
        ));
    }
    
    /**
     * 获取当前用户的OAuth2登录状态
     * @return 用户登录状态的响应
     */
    @GetMapping("/status")
    public ResponseEntity<HttpResponseEntity> getLoginStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> status = new HashMap<>();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && !authentication.getName().equals("anonymousUser")) {
                status.put("authenticated", true);
                status.put("user", authentication.getPrincipal());
                logger.info("用户已认证: {}", authentication.getName());
            } else {
                status.put("authenticated", false);
                logger.info("用户未认证");
            }
            
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                status,
                "获取登录状态成功"
            ));
        } catch (Exception e) {
            logger.error("获取登录状态失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "获取登录状态失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 开始GitHub授权流程
     * 将用户重定向到GitHub授权页面
     * @return 重定向视图
     */
    @GetMapping("/authorization/github")
    public String startGithubLogin() {
        logger.info("开始GitHub授权流程，重定向到GitHub");
        return "redirect:/oauth2/authorization/github";
    }
} 