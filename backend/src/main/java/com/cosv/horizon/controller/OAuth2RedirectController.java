package com.cosv.horizon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

/**
 * OAuth2重定向控制器
 * 负责处理OAuth2回调并重定向到正确的API路径
 */
@Controller
public class OAuth2RedirectController {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2RedirectController.class);

    /**
     * 处理GitHub OAuth2回调
     * 将非API路径(/login/oauth2/code/github)重定向到API路径(/api/login/oauth2/code/github)
     * @param code GitHub返回的授权码，可为空
     * @param state 状态参数，用于验证请求，可为空
     * @return 重定向视图
     */
    @GetMapping("/login/oauth2/code/github")
    public RedirectView handleGitHubCallback(@RequestParam(required = false) String code, 
                                           @RequestParam(required = false) String state) {
        // 重定向到正确的API路径，并保留所有参数
        String redirectUrl = "/api/login/oauth2/code/github";
        if (code != null) {
            redirectUrl += "?code=" + code;
            if (state != null) {
                redirectUrl += "&state=" + state;
            }
        } else if (state != null) {
            redirectUrl += "?state=" + state;
        }
        
        logger.info("处理GitHub OAuth2回调，重定向到: {}", redirectUrl);
        return new RedirectView(redirectUrl);
    }
} 