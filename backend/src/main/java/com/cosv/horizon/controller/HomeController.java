package com.cosv.horizon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 首页控制器
 * 处理系统主页和根路径重定向
 */
@Controller
@RequestMapping("/api")
public class HomeController {

    /**
     * 主页访问处理
     * 将用户重定向到登录页面
     * @return 重定向视图
     */
    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/login.html");
    }
} 