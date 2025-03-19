package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.request.CodeVerifyRequest;
import com.cosv.horizon.service.AuthService;
import com.cosv.horizon.utils.RandomNumberUtils;
import com.cosv.horizon.utils.RedisUtils;
import com.cosv.horizon.utils.SendMailUtils;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public boolean generateCode(String email) {
        try {
            String code = RandomNumberUtils.generateRandomNumber(6);
            boolean sendResult = SendMailUtils.sendEmail(email, "验证码", code);
            if (!sendResult) {
                throw new RuntimeException("邮件发送失败");
            }
            
            // 将验证码存入Redis，有效期5分钟
            RedisUtils.set(email, code, 300, 0);
            return true;
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean validateCode(CodeVerifyRequest request) {

        String code = request.getCode();
        String email = request.getEmail();

        String codeInRedis=RedisUtils.get(email,0);
        if(codeInRedis==null){
            return false;
        }

        if(!code.equals(codeInRedis)){
            return false;
        }
        return true;


    }
}
