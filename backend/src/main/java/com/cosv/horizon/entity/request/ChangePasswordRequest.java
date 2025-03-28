package com.cosv.horizon.entity.request;

import lombok.Data;

/**
 * 修改密码请求实体类
 */
@Data
public class ChangePasswordRequest {
    /**
     * 旧密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
} 