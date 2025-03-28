package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.CodeVerifyRequest;
import com.cosv.horizon.entity.request.UserLoginRequest;
import com.cosv.horizon.entity.request.VerifySessionRequest;
import com.cosv.horizon.entity.response.TokenRefreshResponse;
import com.cosv.horizon.service.AuthService;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.JwtUtil;
import com.cosv.horizon.utils.RandomNumberUtils;
import com.cosv.horizon.utils.RedisUtils;
import com.cosv.horizon.utils.SendMailUtils;
import com.cosv.horizon.utils.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

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

        String codeInRedis = RedisUtils.get(email, 0);
        if (codeInRedis == null) {
            return false;
        }

        if (!code.equals(codeInRedis)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String sendVerificationCode(String email) {
        try {
            // 生成6位随机验证码
            String code = RandomNumberUtils.generateRandomNumber(6);
            
            // 发送验证码邮件
            boolean sendResult = SendMailUtils.sendEmail(
                email, 
                "账号注册验证码", 
                "您正在注册账号，验证码是：" + code + "，有效期10分钟，请不要泄露给他人。"
            );
            
            if (!sendResult) {
                throw new RuntimeException("邮件发送失败");
            }
            
            // 创建验证会话，并将验证码与会话关联
            String sessionId = SessionUtils.createVerificationSession(email, code);
            
            return sessionId;
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean verifySession(VerifySessionRequest request) {
        return SessionUtils.verifyCode(request.getSessionId(), request.getCode());
    }
    
    @Override
    public User login(UserLoginRequest request) {
        // 1. 通过邮箱查找用户
        User user = userService.findByEmail(request.getEmail());
        
        // 2. 如果用户不存在，返回null
        if (user == null) {
            return null;
        }
        
        // 3. 验证密码
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isPasswordMatch) {
            return null;
        }
        
        // 4. 登录成功，返回用户信息
        return user;
    }
    
    @Override
    public TokenRefreshResponse refreshToken(String refreshToken) {
        // 1. 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            return null;
        }
        
        // 2. 确认是刷新令牌而不是访问令牌
        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            return null;
        }
        
        // 3. 从令牌中提取用户信息
        String username = jwtUtil.extractUsername(refreshToken);
        String userUuid = jwtUtil.extractUserUuid(refreshToken);
        
        // 4. 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(username, userUuid);
        
        // 5. 检查刷新令牌是否需要刷新
        if (jwtUtil.needsRefresh(refreshToken)) {
            // 如果距离过期不足12小时，同时刷新刷新令牌
            String newRefreshToken = jwtUtil.generateRefreshToken(username, userUuid);
            return new TokenRefreshResponse(newAccessToken, newRefreshToken);
        } else {
            // 否则只刷新访问令牌
            return new TokenRefreshResponse(newAccessToken, null);
        }
    }
    
    @Override
    public boolean logout(String userUuid) {
        try {
            // 使该用户的所有令牌失效
            jwtUtil.invalidateUserTokens(userUuid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
