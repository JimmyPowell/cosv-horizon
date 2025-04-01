package com.cosv.horizon.controller;

import com.cosv.horizon.entity.Constans;
import com.cosv.horizon.entity.HttpResponseEntity;
import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.response.OrganizationResponse;
import com.cosv.horizon.service.OrganizationService;
import com.cosv.horizon.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员组织控制器
 * 管理员操作组织的接口
 */
@RestController
@RequestMapping("/api/admin/organizations")
public class AdminOrganizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminOrganizationController.class);
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取待审核的组织列表
     * @param authHeader 认证头
     * @return 待审核组织列表
     */
    @GetMapping("/pending")
    public ResponseEntity<HttpResponseEntity> getPendingOrganizations(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户信息
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 检查是否为管理员
            if (!"ADMIN".equals(currentUser.getRole())) {
                logger.warn("非管理员用户 {} 尝试访问管理员接口", currentUser.getName());
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.AUTH_ERROR_CODE,
                        null,
                        "权限不足"
                ));
            }
            
            // 获取待审核组织
            List<Organization> pendingOrganizations = organizationService.getPendingOrganizations();
            
            // 转换为响应对象
            List<OrganizationResponse> responses = pendingOrganizations.stream()
                    .map(org -> organizationService.convertToResponse(org, currentUser.getId()))
                    .collect(Collectors.toList());
            
            logger.info("管理员 {} 获取待审核组织列表，共 {} 个组织", currentUser.getName(), responses.size());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    responses,
                    "获取待审核组织列表成功"
            ));
            
        } catch (Exception e) {
            logger.error("获取待审核组织列表失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "获取待审核组织列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 审核组织请求
     * @param id 组织ID
     * @param approved 是否批准
     * @param rejectReason 拒绝原因
     * @param authHeader 认证头
     * @return 审核结果
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<HttpResponseEntity> reviewOrganization(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectReason,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户信息
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 检查是否为管理员
            if (!"ADMIN".equals(currentUser.getRole())) {
                logger.warn("非管理员用户 {} 尝试访问管理员接口", currentUser.getName());
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.AUTH_ERROR_CODE,
                        null,
                        "权限不足"
                ));
            }
            
            // 如果拒绝但没有提供拒绝原因
            if (!approved && (rejectReason == null || rejectReason.trim().isEmpty())) {
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.FAIL_CODE,
                        null,
                        "拒绝审核时必须提供拒绝原因"
                ));
            }
            
            // 执行审核
            boolean success = organizationService.reviewOrganization(id, approved, rejectReason, currentUser.getId());
            
            if (success) {
                logger.info("管理员 {} 审核组织 ID: {}，结果: {}", currentUser.getName(), id, approved ? "通过" : "拒绝");
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.SUCCESS_CODE,
                        null,
                        "组织审核" + (approved ? "通过" : "拒绝") + "成功"
                ));
            } else {
                logger.warn("管理员 {} 审核组织 ID: {} 失败", currentUser.getName(), id);
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.FAIL_CODE,
                        null,
                        "组织审核操作失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("审核组织失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "审核组织失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有组织列表
     * @param authHeader 认证头
     * @return 所有组织列表
     */
    @GetMapping
    public ResponseEntity<HttpResponseEntity> getAllOrganizations(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户信息
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 检查是否为管理员
            if (!"ADMIN".equals(currentUser.getRole())) {
                logger.warn("非管理员用户 {} 尝试访问管理员接口", currentUser.getName());
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.AUTH_ERROR_CODE,
                        null,
                        "权限不足"
                ));
            }
            
            // TODO: 实现获取所有组织的功能
            // 这里需要在OrganizationService中添加一个方法来获取所有组织
            
            logger.info("管理员 {} 获取所有组织列表", currentUser.getName());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    null,
                    "获取所有组织列表功能待实现"
            ));
            
        } catch (Exception e) {
            logger.error("获取所有组织列表失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "获取所有组织列表失败: " + e.getMessage()
            ));
        }
    }
} 