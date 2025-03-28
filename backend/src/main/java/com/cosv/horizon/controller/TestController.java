package com.cosv.horizon.controller;

import com.cosv.horizon.utils.SendMailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试功能控制器
 * 提供系统测试及调试相关的接口
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    /**
     * 健康检查接口
     * 用于测试系统是否正常运行
     * @return 包含状态信息的响应
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("status", "ok");
        logger.info("Ping测试");
        return ResponseEntity.ok(response);
    }

    /**
     * 邮件发送测试接口
     * 用于测试邮件发送功能是否正常
     * @param to 收件人邮箱地址
     * @param title 邮件标题
     * @param content 邮件内容
     * @return 发送结果信息
     */
    @GetMapping("/send-mail")
    public String sendMail(@RequestParam String to, @RequestParam String title, @RequestParam String content) {
        boolean result = SendMailUtils.sendEmail(to, title, content);
        logger.info("发送邮件到: {}, 结果: {}", to, result ? "成功" : "失败");
        return result ? "邮件发送成功" : "邮件发送失败";
    }
}
