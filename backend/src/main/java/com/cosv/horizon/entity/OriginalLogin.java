package com.cosv.horizon.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户关联的原始登录信息，用于第三方登录
 */
@Data
public class OriginalLogin implements Serializable {
    
    private Long id;           // 自增主键
    private Long userId;       // 关联的用户ID，逻辑外键 -> user(id)
    private String source;     // 登录来源（如GitHub, Google等）
    private String name;       // 在第三方平台的用户名
    
    // 构造函数
    public OriginalLogin() {
    }
    
    public OriginalLogin(Long userId, String source, String name) {
        this.userId = userId;
        this.source = source;
        this.name = name;
    }
} 