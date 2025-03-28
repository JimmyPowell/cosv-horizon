package com.cosv.horizon.service;

import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.ChangePasswordRequest;
import com.cosv.horizon.entity.request.UpdateUserInfoRequest;
import com.cosv.horizon.entity.response.HttpResponseEntity;

public interface UserService {
    /**
     * 注册新用户
     * @param user 用户信息
     * @return 是否注册成功
     */
    boolean register(User user);
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    User findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象，如果不存在则返回null
     */
    User findByEmail(String email);
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    User findById(Long id);
    
    /**
     * 根据UUID查找用户
     * @param uuid 用户UUID
     * @return 用户对象，如果不存在则返回null
     */
    User findByUuid(String uuid);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param newPassword 新密码（已加密）
     * @return 是否更新成功
     */
    boolean updatePassword(Long userId, String newPassword);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否更新成功
     */
    boolean updateUserInfo(User user);
    
    /**
     * 根据认证令牌获取用户信息
     * @param authHeader 认证头
     * @return 包含用户信息或错误信息的响应实体
     */
    HttpResponseEntity getUserInfoByToken(String authHeader);
    
    /**
     * 根据认证令牌更新用户信息
     * @param request 更新用户信息请求
     * @param authHeader 认证头
     * @return 包含更新结果的响应实体
     */
    HttpResponseEntity updateUserInfoByToken(UpdateUserInfoRequest request, String authHeader);
    
    /**
     * 根据认证令牌修改用户密码
     * @param request 修改密码请求
     * @param authHeader 认证头
     * @return 包含修改结果的响应实体
     */
    HttpResponseEntity changePasswordByToken(ChangePasswordRequest request, String authHeader);
}
