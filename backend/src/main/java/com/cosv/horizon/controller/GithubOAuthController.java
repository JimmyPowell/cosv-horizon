package com.cosv.horizon.controller;

import com.cosv.horizon.entity.dto.GithubUserDTO;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.entity.response.UserLoginResponse;
import com.cosv.horizon.service.GithubOAuthService;
import com.cosv.horizon.utils.Constans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

/**
 * GitHub OAuth控制器
 * 处理GitHub OAuth认证流程的请求
 */
@Controller
@Slf4j
public class GithubOAuthController {

    @Autowired
    private GithubOAuthService githubOAuthService;

    /**
     * 跳转到GitHub登录授权页面
     * @return 重定向到GitHub授权URL
     */
    @GetMapping("/oauth/github")
    public RedirectView loginPage() {
        String authorizationUrl = githubOAuthService.getAuthorizationUrl();
        log.info("重定向到GitHub授权页面: {}", authorizationUrl);
        return new RedirectView(authorizationUrl);
    }

    /**
     * 处理GitHub OAuth回调 - 原路径
     * @param code 授权码
     * @return 登录响应或错误信息
     */
    @GetMapping("/oauth/github/callback")
    @ResponseBody
    public ResponseEntity<HttpResponseEntity> callback(@RequestParam String code) {
        log.info("收到GitHub OAuth回调（原路径），code={}", code);
        return processOAuthCallback(code);
    }
    
    /**
     * 处理GitHub OAuth回调 - Spring Security默认路径，不带/api前缀
     * 注意：需要在application.properties中设置server.servlet.context-path=/
     * 或者通过Nginx/其他代理将这个路径映射到此应用
     * @param code 授权码
     * @return 登录响应或错误信息
     */
    @GetMapping("/login/oauth2/code/github")
    @ResponseBody
    public ResponseEntity<HttpResponseEntity> callbackSpringDefault(@RequestParam String code) {
        log.info("收到GitHub OAuth回调（Spring默认路径，不带/api前缀），code={}", code);
        return processOAuthCallback(code);
    }
    
    /**
     * 处理OAuth回调的通用方法
     * @param code 授权码
     * @return 登录响应或错误信息
     */
    private ResponseEntity<HttpResponseEntity> processOAuthCallback(String code) {
        try {
            // 获取访问令牌
            log.info("开始获取GitHub访问令牌...");
            String accessToken = githubOAuthService.getAccessToken(code);
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("获取GitHub访问令牌失败");
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "获取访问令牌失败"
                ));
            }
            log.info("成功获取GitHub访问令牌: {}", accessToken.substring(0, Math.min(10, accessToken.length())) + "...");
            
            // 获取用户信息
            log.info("开始获取GitHub用户信息...");
            GithubUserDTO githubUser = githubOAuthService.getUserInfo(accessToken);
            if (githubUser == null) {
                log.error("获取GitHub用户信息失败");
                return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "获取用户信息失败"
                ));
            }
            log.info("成功获取GitHub用户信息: id={}, login={}, name={}, email={}",
                    githubUser.getId(), githubUser.getLogin(), githubUser.getName(), githubUser.getEmail());
            
            // 处理登录
            log.info("开始处理用户登录/注册...");
            UserLoginResponse loginResponse = githubOAuthService.handleLogin(githubUser);
            log.info("GitHub用户登录/注册成功，生成令牌: {}", 
                    loginResponse.getAccessToken().substring(0, Math.min(10, loginResponse.getAccessToken().length())) + "...");
            
            // 构建响应对象
            HttpResponseEntity responseEntity = new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                loginResponse,
                "登录成功"
            );
            
            // 打印完整响应到控制台
            log.info("完整的登录响应: {}", responseEntity);
            return ResponseEntity.ok(responseEntity);
            
        } catch (Exception e) {
            log.error("GitHub OAuth认证失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.AUTH_ERROR_CODE,
                null,
                "GitHub认证失败: " + e.getMessage()
            ));
        }
    }
}