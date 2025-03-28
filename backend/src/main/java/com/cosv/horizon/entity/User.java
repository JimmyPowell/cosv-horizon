package com.cosv.horizon.entity;

import java.util.Date;
import lombok.Data;
import com.cosv.horizon.enums.UserStatus;

/**
 * 用户实体类
 */
@Data
public class User {

    private Long id;              // 用户ID，自增主键
    private String uuid;          // 用户唯一标识符
    private String name;          // 用户名称，不可为空且唯一
    private String password;      // 用户密码
    private String role;          // 用户全局角色
    private String email;         // 用户电子邮箱
    private String avatar;        // 用户头像路径
    private String company;       // 用户所属公司
    private String location;      // 用户所在地
    private String gitHub;        // GitHub链接
    private UserStatus status;    // 用户状态（枚举：正常、待认证、删除、封禁）
    private Long rating;          // 用户评分
    private String website;       // 用户个人网站
    private String freeText;      // 用户自由描述文本
    private String realName;      // 用户真实姓名
    private Date createDate;      // 创建日期，默认为当前时间
    private Date updateDate;      // 更新日期，自动更新
    
    // GitHub OAuth相关字段
    private String githubId;      // GitHub账号唯一ID
    private String githubLogin;   // GitHub登录名
    private String githubToken;   // GitHub访问令牌

}
