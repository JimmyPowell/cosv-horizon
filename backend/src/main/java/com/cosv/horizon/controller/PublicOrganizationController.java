package com.cosv.horizon.controller;

import com.cosv.horizon.entity.Constans;
import com.cosv.horizon.entity.HttpResponseEntity;
import com.cosv.horizon.entity.Organization;
import com.cosv.horizon.entity.UserOrganizationLink;
import com.cosv.horizon.entity.response.PublicOrganizationResponse;
import com.cosv.horizon.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公开组织控制器
 * 提供不需要认证的组织查询接口
 */
@RestController
@RequestMapping("/api/public/organizations")
public class PublicOrganizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(PublicOrganizationController.class);
    
    @Autowired
    private OrganizationService organizationService;
    
    /**
     * 获取所有活跃状态的组织列表
     * @return 组织列表
     */
    @GetMapping
    public ResponseEntity<HttpResponseEntity> getAllActiveOrganizations() {
        try {
            // 获取所有活跃组织
            List<Organization> organizations = organizationService.getAllActiveOrganizations();
            
            // 转换为公开响应对象
            List<PublicOrganizationResponse> responses = organizations.stream()
                    .map(org -> {
                        // 获取成员数量
                        List<UserOrganizationLink> members = organizationService.getOrganizationMembers(org.getId());
                        return new PublicOrganizationResponse(org, members.size());
                    })
                    .collect(Collectors.toList());
            
            logger.info("查询所有活跃组织列表，共 {} 个组织", responses.size());
            
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
     * 获取指定组织的公开信息
     * @param id 组织ID
     * @return 组织详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<HttpResponseEntity> getOrganizationById(@PathVariable Long id) {
        try {
            // 获取活跃状态的组织
            Organization organization = organizationService.getPublicOrganizationById(id);
            if (organization == null) {
                return ResponseEntity.ok(new HttpResponseEntity(
                        Constans.NO_CONTENT_CODE,
                        null,
                        "组织不存在或未激活"
                ));
            }
            
            // 获取成员数量
            List<UserOrganizationLink> members = organizationService.getOrganizationMembers(id);
            
            // 转换为公开响应对象
            PublicOrganizationResponse response = new PublicOrganizationResponse(organization, members.size());
            
            logger.info("查看组织 {} 公开信息", organization.getName());
            
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
} 