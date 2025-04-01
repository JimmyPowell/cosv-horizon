package com.cosv.horizon.controller;

import com.cosv.horizon.entity.Constans;
import com.cosv.horizon.entity.HttpResponseEntity;
import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.OrganizationStatus;
import com.cosv.horizon.entity.User;
import com.cosv.horizon.entity.request.OrganizationCreateRequest;
import com.cosv.horizon.entity.response.OrganizationResponse;
import com.cosv.horizon.service.OrganizationService;
import com.cosv.horizon.service.UserService;
import com.cosv.horizon.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织控制器
 * 处理组织的创建、修改、删除等操作
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
    
    @Autowired
    private OrganizationService organizationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建组织
     * @param request 组织创建请求
     * @param authHeader 认证头
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<HttpResponseEntity> createOrganization(
            @RequestBody OrganizationCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 创建组织
            Organization organization = organizationService.createOrganization(request, currentUser.getId());
            
            // 转换为响应对象
            OrganizationResponse response = organizationService.convertToResponse(organization, currentUser.getId());
            
            logger.info("用户 {} 创建组织 {} 成功，状态: {}", currentUser.getName(), organization.getName(), organization.getStatus());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    response,
                    "组织创建成功，等待管理员审核"
            ));
            
        } catch (Exception e) {
            logger.error("创建组织失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.EXIST_CODE,
                    null,
                    "创建组织失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户的组织列表
     * @param authHeader 认证头
     * @return 组织列表
     */
    @GetMapping("/my")
    public ResponseEntity<HttpResponseEntity> getUserOrganizations(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 获取用户组织
            List<Organization> organizations = organizationService.getUserOrganizations(currentUser.getId());
            
            // 转换为响应对象
            List<OrganizationResponse> responses = organizations.stream()
                    .map(org -> organizationService.convertToResponse(org, currentUser.getId()))
                    .collect(Collectors.toList());
            
            logger.info("获取用户 {} 的组织列表，共 {} 个组织", currentUser.getName(), responses.size());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    responses,
                    "获取组织列表成功"
            ));
            
        } catch (Exception e) {
            logger.error("获取组织列表失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "获取组织列表失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取组织详情
     * @param id 组织ID
     * @param authHeader 认证头
     * @return 组织详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<HttpResponseEntity> getOrganizationById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 获取组织
            Organization organization = organizationService.getOrganizationById(id);
            if (organization == null) {
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.NO_CONTENT_CODE,
                        null,
                        "组织不存在"
                ));
            }
            
            // 转换为响应对象
            OrganizationResponse response = organizationService.convertToResponse(organization, currentUser.getId());
            
            logger.info("用户 {} 查看组织 {} 详情", currentUser.getName(), organization.getName());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    response,
                    "获取组织详情成功"
            ));
            
        } catch (Exception e) {
            logger.error("获取组织详情失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "获取组织详情失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新组织信息
     * @param id 组织ID
     * @param request 更新请求
     * @param authHeader 认证头
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<HttpResponseEntity> updateOrganization(
            @PathVariable Long id,
            @RequestBody OrganizationCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 更新组织
            Organization organization = organizationService.updateOrganization(id, request, currentUser.getId());
            
            // 转换为响应对象
            OrganizationResponse response = organizationService.convertToResponse(organization, currentUser.getId());
            
            logger.info("用户 {} 更新组织 {} 信息成功", currentUser.getName(), organization.getName());
            
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.SUCCESS_CODE,
                    response,
                    "更新组织信息成功"
            ));
            
        } catch (Exception e) {
            logger.error("更新组织信息失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "更新组织信息失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 更新组织状态
     * @param id 组织ID
     * @param status 新状态
     * @param authHeader 认证头
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<HttpResponseEntity> updateOrganizationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 检查状态是否有效
            if (!OrganizationStatus.isValid(status)) {
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.FAIL_CODE,
                        null,
                        "无效的组织状态"
                ));
            }
            
            // 更新状态
            boolean success = organizationService.updateOrganizationStatus(id, status, currentUser.getId());
            
            if (success) {
                logger.info("用户 {} 更新组织 ID {} 状态为 {} 成功", currentUser.getName(), id, status);
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.SUCCESS_CODE,
                        null,
                        "更新组织状态成功"
                ));
            } else {
                logger.warn("用户 {} 更新组织 ID {} 状态失败", currentUser.getName(), id);
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.FAIL_CODE,
                        null,
                        "更新组织状态失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("更新组织状态失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "更新组织状态失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除组织
     * @param id 组织ID
     * @param authHeader 认证头
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpResponseEntity> deleteOrganization(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证登录状态并获取用户ID
            HttpResponseEntity userResponse = userService.getUserInfoByToken(authHeader);
            if (userResponse.getCode() != Constans.SUCCESS_CODE) {
                return ResponseEntity.ok(userResponse);
            }
            
            User currentUser = (User) userResponse.getData();
            
            // 删除组织
            boolean success = organizationService.deleteOrganization(id, currentUser.getId());
            
            if (success) {
                logger.info("用户 {} 删除组织 ID {} 成功", currentUser.getName(), id);
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.SUCCESS_CODE,
                        null,
                        "删除组织成功"
                ));
            } else {
                logger.warn("用户 {} 删除组织 ID {} 失败", currentUser.getName(), id);
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.FAIL_CODE,
                        null,
                        "删除组织失败"
                ));
            }
            
        } catch (Exception e) {
            logger.error("删除组织失败", e);
            return ResponseEntity.ok(new HttpResponseEntity(
                    Constans.FAIL_CODE,
                    null,
                    "删除组织失败: " + e.getMessage()
            ));
        }
    }
} 