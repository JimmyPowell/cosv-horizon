package com.cosv.horizon.controller;

import com.cosv.horizon.utils.SendMailUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/send-mail")
    public String sendMail(@RequestParam String to, @RequestParam String title, @RequestParam String content) {
        boolean result = SendMailUtils.sendEmail(to, title, content);
        return result ? "邮件发送成功" : "邮件发送失败";
    }
}
