package com.cosv.horizon.controller;

import com.cosv.horizon.entity.request.ChangePasswordRequest;
import com.cosv.horizon.entity.request.UpdateUserInfoRequest;
import com.cosv.horizon.entity.response.HttpResponseEntity;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.Constans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户信息
     * @param authHeader 认证头
     * @return 用户信息
     */
    @GetMapping("info")
    public ResponseEntity<HttpResponseEntity> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            HttpResponseEntity response = userService.getUserInfoByToken(authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户信息时发生异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "获取用户信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新用户信息
     * @param request 更新用户信息请求
     * @param authHeader 认证头
     * @return 更新结果
     */
    @PutMapping("info")
    public ResponseEntity<HttpResponseEntity> updateUserInfo(
            @RequestBody UpdateUserInfoRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            HttpResponseEntity response = userService.updateUserInfoByToken(request, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新用户信息时发生异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "更新用户信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 修改用户密码
     * @param request 修改密码请求
     * @param authHeader 认证头
     * @return 修改结果
     */
    @PostMapping("change-password")
    public ResponseEntity<HttpResponseEntity> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            HttpResponseEntity response = userService.changePasswordByToken(request, authHeader);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("修改密码时发生异常", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                Constans.EXIST_CODE,
                null,
                "修改密码失败: " + e.getMessage()
            ));
        }
    }
} 