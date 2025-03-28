package com.cosv.horizon.utils;

import org.springframework.stereotype.Component;

/**
 * 会话工具类，用于管理验证会话
 */
@Component
public class SessionUtils {
    
    private static final String SESSION_PREFIX = "verify:session:";
    private static final int SESSION_TTL = 600; // 10分钟
    
    /**
     * 生成唯一的会话ID
     * @return 会话ID
     */
    public static String generateSessionId() {
        return UUIDUtils.generateUUIDWithoutHyphens();
    }
    
    /**
     * 创建验证会话
     * @param email 用户邮箱
     * @param code 验证码
     * @return 会话ID
     */
    public static String createVerificationSession(String email, String code) {
        String sessionId = generateSessionId();
        // 在Redis中存储会话信息，TTL为10分钟
        String sessionKey = SESSION_PREFIX + sessionId;
        
        // 使用Hash结构存储
        RedisUtils.hset(sessionKey, "email", email, 0);
        RedisUtils.hset(sessionKey, "code", code, 0);
        RedisUtils.hset(sessionKey, "verified", "false", 0);
        
        // 设置一个临时值来让过期时间生效
        RedisUtils.set(sessionKey + ":ttl", "1", SESSION_TTL, 0);
        
        return sessionId;
    }
    
    /**
     * 验证验证码
     * @param sessionId 会话ID
     * @param code 用户输入的验证码
     * @return 是否验证成功
     */
    public static boolean verifyCode(String sessionId, String code) {
        String sessionKey = SESSION_PREFIX + sessionId;
        
        // 检查会话是否存在 - 通过验证其TTL键是否存在
        if (!RedisUtils.exists(sessionKey + ":ttl", 0)) {
            return false; // 会话不存在或已过期
        }
        
        // 获取存储的验证码
        String storedCode = RedisUtils.hget(sessionKey, "code", 0);
        if (storedCode == null) {
            return false; // 验证码不存在
        }
        
        if (code.equals(storedCode)) {
            // 更新会话状态为已验证
            RedisUtils.hset(sessionKey, "verified", "true", 0);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查会话是否已验证
     * @param sessionId 会话ID
     * @return 是否已验证
     */
    public static boolean isSessionVerified(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        
        // 检查会话是否存在 - 通过验证其TTL键是否存在
        if (!RedisUtils.exists(sessionKey + ":ttl", 0)) {
            return false; // 会话不存在或已过期
        }
        
        // 检查会话是否已验证
        String verified = RedisUtils.hget(sessionKey, "verified", 0);
        return "true".equals(verified);
    }
    
    /**
     * 获取会话关联的邮箱
     * @param sessionId 会话ID
     * @return 邮箱地址
     */
    public static String getSessionEmail(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        return RedisUtils.hget(sessionKey, "email", 0);
    }
    
    /**
     * 清除会话
     * @param sessionId 会话ID
     */
    public static void clearSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        RedisUtils.del(sessionKey, 0);
        RedisUtils.del(sessionKey + ":ttl", 0);
    }
} 