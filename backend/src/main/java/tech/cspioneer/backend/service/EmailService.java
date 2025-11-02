package tech.cspioneer.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${mail.mock:false}")
    private boolean mock;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String to, String code, int ttlMinutes) {
        if (mock) {
            log.info("[MOCK MAIL] to={}, code={}, ttl={}m", to, code, ttlMinutes);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setSubject("COSV Horizon 验证码");
            message.setText("您的验证码是 " + code + "，有效期 " + ttlMinutes + " 分钟。如非本人操作请忽略。");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("发送邮件失败: to={}", to, e);
            throw new RuntimeException("发送邮件失败");
        }
    }
}
