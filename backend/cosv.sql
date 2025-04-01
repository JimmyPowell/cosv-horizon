-- COSV数据库表结构
-- 此脚本用于创建COSV_HORIZON（Common Open Source Vulnerability）功能所需的所有数据库表
-- 注意：所有外键关系均采用逻辑外键（无物理约束）以提高性能，通过注释和索引说明关系

-- 创建cosv_horizon数据库（用于存放所有相关表）
CREATE DATABASE IF NOT EXISTS cosv_horizon;
USE cosv_horizon;

-- 创建user表（存储用户信息）
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 用户ID，自增主键
                      name VARCHAR(255) NOT NULL UNIQUE,       -- 用户名称，不可为空且唯一
                      password VARCHAR(255),                   -- 用户密码
                      role VARCHAR(50),                        -- 用户全局角色
                      email VARCHAR(255),                      -- 用户电子邮箱
                      avatar VARCHAR(255),                     -- 用户头像路径
                      company VARCHAR(255),                    -- 用户所属公司
                      location VARCHAR(255),                   -- 用户所在地
    -- LinkedIn链接
                      git_hub VARCHAR(255),                    -- GitHub链接
    -- Twitter链接
                      status VARCHAR(50) NOT NULL,             -- 用户状态（如CREATED, ACTIVE等）
                      rating BIGINT DEFAULT 0,                 -- 用户评分
                      website VARCHAR(255),                    -- 用户个人网站
                      free_text TEXT,                          -- 用户自由描述文本
                      real_name VARCHAR(255),                  -- 用户真实姓名
                      create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建日期，默认为当前时间
                      update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 更新日期，自动更新
);

-- 创建original_login表（用户关联的原始登录信息，用于第三方登录）
CREATE TABLE original_login (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                user_id BIGINT NOT NULL,                 -- 关联的用户ID，逻辑外键 -> user(id)
                                source VARCHAR(50) NOT NULL,             -- 登录来源（如GitHub, Google等）
                                name VARCHAR(255) NOT NULL,              -- 在第三方平台的用户名
                                UNIQUE (source, name)                    -- 同一来源的同一用户名只能关联一次
);

-- 创建organization表（存储组织信息）
CREATE TABLE organization (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 组织ID，自增主键
                              name VARCHAR(255) NOT NULL UNIQUE,       -- 组织名称，不可为空且唯一
                              status VARCHAR(50) NOT NULL,             -- 组织状态（如ACTIVE, PENDING等）
                              date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 组织创建日期，默认为当前时间
                              avatar VARCHAR(255),                     -- 组织头像路径
                              description TEXT,                        -- 组织描述
                              rating BIGINT DEFAULT 0,                 -- 组织评分
                              free_text TEXT                           -- 组织自由描述文本（根据需求添加）
);

-- 创建lnk_user_organization表（用户与组织的关联关系）
CREATE TABLE lnk_user_organization (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                       organization_id BIGINT NOT NULL,         -- 组织ID，逻辑外键 -> organization(id)
                                       user_id BIGINT NOT NULL,                 -- 用户ID，逻辑外键 -> user(id)
                                       role VARCHAR(50) NOT NULL,               -- 用户在组织中的角色（如ADMIN, MEMBER等）
                                       UNIQUE (organization_id, user_id)        -- 一个用户在一个组织中只能有一个角色
);

-- 创建raw_cosv_file表（存储原始COSV文件信息）
CREATE TABLE raw_cosv_file (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                               file_name VARCHAR(255) NOT NULL,         -- 文件名称
                               user_id BIGINT NOT NULL,                 -- 上传用户ID，逻辑外键 -> user(id)
                               organization_id BIGINT NOT NULL,         -- 所属组织ID，逻辑外键 -> organization(id)
                               status VARCHAR(50) NOT NULL,             -- 文件状态（如UPLOADED, PROCESSED等）
                               status_message TEXT,                     -- 状态消息（如错误信息）
                               content_length BIGINT,                   -- 文件内容长度
                               create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建日期，默认为当前时间
                               update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 更新日期，自动更新
);

-- 创建cosv_file表（存储处理后的COSV文件信息）
CREATE TABLE cosv_file (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                           identifier VARCHAR(255) NOT NULL,        -- COSV文件标识符
                           modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 修改时间，默认为当前时间
                           prev_cosv_file_id BIGINT,                -- 前一个版本的COSV文件ID，逻辑外键 -> cosv_file(id)
                           user_id BIGINT                           -- 创建/更新文件的用户ID，逻辑外键 -> user(id)
);

-- 创建cosv_generated_id表（用于生成COSV漏洞标识符）
CREATE TABLE cosv_generated_id (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                   create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建日期，默认为当前时间
                                   update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 更新日期，自动更新
    -- 此表用于生成形如 COSV-年份-序号 的漏洞ID
);

-- 创建vulnerability_metadata表（存储漏洞元数据信息）
CREATE TABLE vulnerability_metadata (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                        identifier VARCHAR(255) NOT NULL UNIQUE, -- 漏洞标识符（如COSV-2023-1234），唯一
                                        summary VARCHAR(255) NOT NULL,           -- 漏洞摘要
                                        details TEXT NOT NULL,                   -- 漏洞详情
                                        severity_num FLOAT NOT NULL,             -- 严重性评分
                                        modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 修改时间，默认为当前时间
                                        submitted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 提交时间，默认为当前时间
                                        language VARCHAR(50) NOT NULL,           -- 编程语言
                                        status VARCHAR(50) NOT NULL,             -- 漏洞状态
                                        user_id BIGINT NOT NULL,                 -- 提交用户ID，逻辑外键 -> user(id)
                                        organization_id BIGINT,                  -- 关联组织ID，逻辑外键 -> organization(id)
                                        latest_cosv_file_id BIGINT NOT NULL      -- 最新COSV文件ID，逻辑外键 -> cosv_file(id)
);

-- 创建vulnerability_metadata_project表（存储漏洞相关项目信息）
CREATE TABLE vulnerability_metadata_project (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                                name VARCHAR(255) NOT NULL,              -- 项目名称
                                                url VARCHAR(255) NOT NULL,               -- 项目URL
                                                versions TEXT NOT NULL,                  -- 受影响的版本
                                                vulnerability_metadata_id BIGINT NOT NULL, -- 关联的漏洞元数据ID，逻辑外键 -> vulnerability_metadata(id)
                                                type VARCHAR(50) NOT NULL                -- 关联类型
);

-- 创建tag表（存储标签信息）
CREATE TABLE tag (
                     id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                     name VARCHAR(255) NOT NULL UNIQUE,       -- 标签名称，不可重复
                     create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建日期，默认为当前时间
);

-- 创建lnk_vulnerability_metadata_tag表（漏洞与标签的关联关系）
CREATE TABLE lnk_vulnerability_metadata_tag (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                                vulnerability_metadata_id BIGINT NOT NULL, -- 漏洞元数据ID，逻辑外键 -> vulnerability_metadata(id)
                                                tag_id BIGINT NOT NULL,                  -- 标签ID，逻辑外键 -> tag(id)
                                                UNIQUE (vulnerability_metadata_id, tag_id) -- 同一漏洞不能重复关联同一标签
);

-- 创建lnk_vulnerability_metadata_user表（漏洞与用户的关联关系，如贡献者）
CREATE TABLE lnk_vulnerability_metadata_user (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                                 vulnerability_metadata_id BIGINT NOT NULL, -- 漏洞元数据ID，逻辑外键 -> vulnerability_metadata(id)
                                                 user_id BIGINT NOT NULL,                 -- 用户ID，逻辑外键 -> user(id)
                                                 UNIQUE (vulnerability_metadata_id, user_id) -- 同一漏洞不能重复关联同一用户
);

-- 创建索引（用于提高查询性能）
CREATE INDEX idx_user_name ON user(name);                       -- 用户名索引
CREATE INDEX idx_user_status ON user(status);                   -- 用户状态索引

CREATE INDEX idx_organization_name ON organization(name);       -- 组织名索引
CREATE INDEX idx_organization_status ON organization(status);   -- 组织状态索引

-- 逻辑外键索引
CREATE INDEX idx_original_login_user_id ON original_login(user_id); -- 用户登录-用户索引

CREATE INDEX idx_lnk_user_organization_user_id ON lnk_user_organization(user_id); -- 用户-组织关联用户索引
CREATE INDEX idx_lnk_user_organization_organization_id ON lnk_user_organization(organization_id); -- 用户-组织关联组织索引
CREATE INDEX idx_lnk_user_organization_role ON lnk_user_organization(role); -- 角色索引
CREATE INDEX idx_lnk_user_organization_compound ON lnk_user_organization(user_id, organization_id); -- 用户-组织组合索引

CREATE INDEX idx_raw_cosv_file_user_id ON raw_cosv_file(user_id);     -- 原始文件-用户索引
CREATE INDEX idx_raw_cosv_file_status ON raw_cosv_file(status);       -- 原始文件状态索引
CREATE INDEX idx_raw_cosv_file_organization_id ON raw_cosv_file(organization_id); -- 原始文件-组织索引

CREATE INDEX idx_cosv_file_identifier ON cosv_file(identifier);       -- COSV文件标识符索引
CREATE INDEX idx_cosv_file_user_id ON cosv_file(user_id);             -- COSV文件-用户索引
CREATE INDEX idx_cosv_file_prev_file_id ON cosv_file(prev_cosv_file_id); -- COSV文件-前一版本索引

CREATE INDEX idx_vulnerability_metadata_identifier ON vulnerability_metadata(identifier); -- 漏洞标识符索引
CREATE INDEX idx_vulnerability_metadata_language ON vulnerability_metadata(language);     -- 漏洞语言索引
CREATE INDEX idx_vulnerability_metadata_status ON vulnerability_metadata(status);         -- 漏洞状态索引
CREATE INDEX idx_vulnerability_metadata_user_id ON vulnerability_metadata(user_id);       -- 漏洞-用户索引
CREATE INDEX idx_vulnerability_metadata_organization_id ON vulnerability_metadata(organization_id); -- 漏洞-组织索引
CREATE INDEX idx_vulnerability_metadata_latest_file_id ON vulnerability_metadata(latest_cosv_file_id); -- 漏洞-最新文件索引

CREATE INDEX idx_vulnerability_metadata_project_type ON vulnerability_metadata_project(type); -- 项目类型索引
CREATE INDEX idx_vulnerability_metadata_project_vuln_id ON vulnerability_metadata_project(vulnerability_metadata_id); -- 项目-漏洞索引

CREATE INDEX idx_tag_name ON tag(name);                         -- 标签名索引

CREATE INDEX idx_lnk_vulnerability_metadata_tag_vuln_id ON lnk_vulnerability_metadata_tag(vulnerability_metadata_id); -- 漏洞标签-漏洞索引
CREATE INDEX idx_lnk_vulnerability_metadata_tag_tag_id ON lnk_vulnerability_metadata_tag(tag_id); -- 漏洞标签-标签索引
CREATE INDEX idx_lnk_vulnerability_metadata_tag_compound ON lnk_vulnerability_metadata_tag(vulnerability_metadata_id, tag_id); -- 漏洞标签组合索引

CREATE INDEX idx_lnk_vulnerability_metadata_user_vuln_id ON lnk_vulnerability_metadata_user(vulnerability_metadata_id); -- 漏洞用户-漏洞索引
CREATE INDEX idx_lnk_vulnerability_metadata_user_user_id ON lnk_vulnerability_metadata_user(user_id); -- 漏洞用户-用户索引
CREATE INDEX idx_lnk_vulnerability_metadata_user_compound ON lnk_vulnerability_metadata_user(vulnerability_metadata_id, user_id); -- 漏洞用户组合索引

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    target_id BIGINT COMMENT '目标对象ID',
    user_id BIGINT NOT NULL COMMENT '接收者用户ID',
    sender_id BIGINT COMMENT '发送者用户ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time TIMESTAMP NULL COMMENT '过期时间',
    action_url VARCHAR(255) COMMENT '操作链接',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '通知状态'
) COMMENT='通知表';

-- 创建通知表索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_target_id ON notification(target_id);
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON notification(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON notification(create_time);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notification(status);

-- API密钥表
CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_prefix VARCHAR(10) NOT NULL COMMENT '密钥前缀，用于识别',
    key_hash VARCHAR(64) NOT NULL UNIQUE COMMENT '密钥的SHA-256哈希值',
    creator_user_id BIGINT NOT NULL COMMENT '创建密钥的用户ID',
    organization_id BIGINT NULL COMMENT '关联的组织ID，NULL表示个人密钥(PAT)',
    description VARCHAR(255) NULL COMMENT '用户提供的密钥描述',
    scopes TEXT NULL COMMENT '授权范围列表，逗号分隔',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '密钥状态: ACTIVE, REVOKED, EXPIRED',
    last_used_time TIMESTAMP NULL COMMENT '最后成功使用时间',
    last_used_ip VARCHAR(45) NULL COMMENT '最后成功使用的IP地址',
    expire_time TIMESTAMP NULL COMMENT '密钥过期时间，NULL表示永不过期',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) COMMENT='API密钥管理表';

-- 创建API密钥表索引
CREATE INDEX IF NOT EXISTS idx_apikey_prefix ON api_key(key_prefix);
CREATE INDEX IF NOT EXISTS idx_apikey_creator ON api_key(creator_user_id);
CREATE INDEX IF NOT EXISTS idx_apikey_organization ON api_key(organization_id);
CREATE INDEX IF NOT EXISTS idx_apikey_status ON api_key(status);
CREATE INDEX IF NOT EXISTS idx_apikey_expire_time ON api_key(expire_time);