package com.cosv.horizon.service.impl;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.ChangePasswordRequest;
import com.cosv.horizon.entity.request.UpdateUserInfoRequest;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.enums.UserStatus;
import com.cosv.horizon.mapper.UserMapper;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.Constans;
import com.cosv.horizon.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean register(User user) {
        try {
            // 1. 检查用户名是否已存在
            if (findByUsername(user.getName()) != null) {
                logger.warn("注册失败：用户名 {} 已存在", user.getName());
                return false;
            }
            
            // 2. 检查邮箱是否已注册
            if (findByEmail(user.getEmail()) != null) {
                logger.warn("注册失败：邮箱 {} 已注册", user.getEmail());
                return false;
            }
            
            // 3. 设置用户默认值
            user.setPassword(passwordEncoder.encode(user.getPassword())); // 使用BCrypt加密密码
            user.setStatus(UserStatus.NORMAL); // 设置为正常状态
            user.setRating(0L); // 初始评分为0
            
            // 4. 生成UUID
            user.setUuid(UUID.randomUUID().toString());
            
            // 5. 设置时间
            Date now = new Date();
            user.setCreateDate(now);
            user.setUpdateDate(now);
            
            // 6. 保存用户
            userMapper.insert(user);
            logger.info("用户 {} 注册成功", user.getName());
            return true;
        } catch (Exception e) {
            logger.error("用户注册失败", e);
            return false;
        }
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }
    
    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    
    @Override
    public User findByUuid(String uuid) {
        return userMapper.findByUuid(uuid);
    }
    
    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        try {
            // 获取当前时间
            Date now = new Date();
            
            // 更新密码和更新时间
            int result = userMapper.updatePassword(userId, newPassword, now);
            
            // 如果影响行数大于0，则更新成功
            boolean success = result > 0;
            if (success) {
                logger.info("用户ID {} 密码更新成功", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("更新密码失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateUserInfo(User user) {
        try {
            // 获取当前时间
            Date now = new Date();
            user.setUpdateDate(now);
            
            // 更新用户信息
            int result = userMapper.updateUserInfo(user);
            
            // 如果影响行数大于0，则更新成功
            boolean success = result > 0;
            if (success) {
                logger.info("用户ID {} 信息更新成功", user.getId());
            }
            return success;
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return false;
        }
    }

    @Override
    public HttpResponseEntity getUserInfoByToken(String authHeader) {
        try {
            // 1. 从认证头中提取令牌
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                logger.warn("无效的认证头");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "无效的认证头"
                );
            }
            
            // 2. 验证令牌
            if (!jwtUtil.validateToken(token)) {
                logger.warn("令牌验证失败");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    Constans.TOKEN_ERROR
                );
            }
            
            // 3. 从令牌中提取用户UUID
            String userUuid = jwtUtil.extractUserUuid(token);
            
            // 4. 获取用户信息
            User user = findByUuid(userUuid);
            if (user == null) {
                logger.warn("用户不存在，UUID: {}", userUuid);
                return new HttpResponseEntity(
                    Constans.NO_USER_CODE,
                    null,
                    Constans.NO_USER
                );
            }
            
            // 5. 移除敏感信息
            user.setPassword(null);
            
            // 6. 返回用户信息
            logger.info("获取用户信息成功，用户ID: {}", user.getId());
            return new HttpResponseEntity(
                Constans.SUCCESS_CODE,
                user,
                "获取用户信息成功"
            );
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "获取用户信息失败: " + e.getMessage()
            );
        }
    }
    
    @Override
    public HttpResponseEntity updateUserInfoByToken(UpdateUserInfoRequest request, String authHeader) {
        try {
            // 1. 从认证头中提取令牌
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                logger.warn("无效的认证头");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "无效的认证头"
                );
            }
            
            // 2. 验证令牌
            if (!jwtUtil.validateToken(token)) {
                logger.warn("令牌验证失败");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    Constans.TOKEN_ERROR
                );
            }
            
            // 3. 从令牌中提取用户UUID
            String userUuid = jwtUtil.extractUserUuid(token);
            
            // 4. 获取用户信息
            User user = findByUuid(userUuid);
            if (user == null) {
                logger.warn("用户不存在，UUID: {}", userUuid);
                return new HttpResponseEntity(
                    Constans.NO_USER_CODE,
                    null,
                    Constans.NO_USER
                );
            }
            
            // 5. 更新用户信息
            user.setAvatar(request.getAvatar());
            user.setCompany(request.getCompany());
            user.setLocation(request.getLocation());
            user.setGitHub(request.getGitHub());
            user.setWebsite(request.getWebsite());
            user.setFreeText(request.getFreeText());
            user.setRealName(request.getRealName());
            
            // 6. 保存更新
            boolean result = updateUserInfo(user);
            
            // 7. 返回结果
            if (result) {
                // 移除敏感信息
                user.setPassword(null);
                logger.info("用户信息更新成功，用户ID: {}", user.getId());
                return new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    user,
                    "用户信息更新成功"
                );
            } else {
                logger.warn("用户信息更新失败，用户ID: {}", user.getId());
                return new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "用户信息更新失败"
                );
            }
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "用户信息更新失败: " + e.getMessage()
            );
        }
    }
    
    @Override
    public HttpResponseEntity changePasswordByToken(ChangePasswordRequest request, String authHeader) {
        try {
            // 1. 从认证头中提取令牌
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                logger.warn("无效的认证头");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    "无效的认证头"
                );
            }
            
            // 2. 验证令牌
            if (!jwtUtil.validateToken(token)) {
                logger.warn("令牌验证失败");
                return new HttpResponseEntity(
                    Constans.AUTH_ERROR_CODE,
                    null,
                    Constans.TOKEN_ERROR
                );
            }
            
            // 3. 从令牌中提取用户UUID
            String userUuid = jwtUtil.extractUserUuid(token);
            
            // 4. 获取用户信息
            User user = findByUuid(userUuid);
            if (user == null) {
                logger.warn("用户不存在，UUID: {}", userUuid);
                return new HttpResponseEntity(
                    Constans.NO_USER_CODE,
                    null,
                    Constans.NO_USER
                );
            }
            
            // 5. 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                logger.warn("旧密码不正确，用户ID: {}", user.getId());
                return new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "旧密码不正确"
                );
            }
            
            // 6. 更新密码
            boolean result = updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
            
            // 7. 使所有令牌失效
            jwtUtil.invalidateUserTokens(user.getUuid());
            
            // 8. 返回结果
            if (result) {
                logger.info("密码修改成功，用户ID: {}", user.getId());
                return new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    null,
                    "密码修改成功，请重新登录"
                );
            } else {
                logger.warn("密码修改失败，用户ID: {}", user.getId());
                return new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "密码修改失败"
                );
            }
        } catch (Exception e) {
            logger.error("密码修改失败", e);
            return new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "密码修改失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 从认证头中提取令牌
     * @param authHeader 认证头
     * @return 令牌，如果格式无效返回null
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
