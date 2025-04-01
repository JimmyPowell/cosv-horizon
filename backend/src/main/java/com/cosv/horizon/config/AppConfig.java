package com.cosv.horizon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用程序配置类
 * 包含各种Bean配置
 */
@Configuration
public class AppConfig {

    /**
     * 创建RestTemplate实例
     * 用于发送HTTP请求到GitHub OAuth API
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 