package com.cosv.horizon.entity.request;

import lombok.Data;

/**
 * 更新用户信息请求实体类
 */
@Data
public class UpdateUserInfoRequest {
    private String avatar;        // 用户头像路径
    private String company;       // 用户所属公司
    private String location;      // 用户所在地
    private String gitHub;        // GitHub链接
    private String website;       // 用户个人网站
    private String freeText;      // 用户自由描述文本
    private String realName;      // 用户真实姓名
} 