-- COSV数据库表结构
-- 此脚本用于创建COSV_HORIZON（Common Open Source Vulnerability）功能所需的所有数据库表
-- 更新日期: 2025-10-18
-- 注意：所有外键关系均采用逻辑外键（无物理约束）以提高性能，通过注释和索引说明关系

-- 创建cosv_horizon数据库（用于存放所有相关表）
CREATE DATABASE IF NOT EXISTS cosv_horizon;
USE cosv_horizon;

-- 创建user表（存储用户信息）
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 用户ID，自增主键
                      uuid VARCHAR(36) NOT NULL UNIQUE,         -- 用户全局唯一标识符
                      name VARCHAR(255) NOT NULL UNIQUE,       -- 用户名称，不可为空且唯一
                      password VARCHAR(255),                   -- 用户密码
                      role VARCHAR(50),                        -- 用户全局角色
                      email VARCHAR(255) NOT NULL,             -- 用户电子邮箱
                      avatar VARCHAR(255),                     -- 用户头像路径
                      company VARCHAR(255),                    -- 用户所属公司
                      location VARCHAR(255),                   -- 用户所在地
                      git_hub VARCHAR(255),                    -- GitHub链接
                      status VARCHAR(50) NOT NULL,             -- 用户状态（如CREATED, ACTIVE等）
                      rating BIGINT DEFAULT 0,                 -- 用户评分
                      website VARCHAR(255),                    -- 用户个人网站
                      free_text TEXT,                          -- 用户自由描述文本
                      real_name VARCHAR(255),                  -- 用户真实姓名
                      create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建日期，默认为当前时间
                      update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 更新日期，自动更新
                      CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'USER', 'MODERATOR')),
                      CONSTRAINT chk_user_status CHECK (status IN ('CREATED', 'ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- 创建original_login表（用户关联的原始登录信息，用于第三方登录）
CREATE TABLE original_login (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                uuid VARCHAR(36) NOT NULL UNIQUE,         -- 登录信息全局唯一标识符
                                user_id BIGINT NOT NULL,                 -- 关联的用户ID，逻辑外键 -> user(id)
                                source VARCHAR(50) NOT NULL,             -- 登录来源（如GitHub, Google等）
                                name VARCHAR(255) NOT NULL,              -- 在第三方平台的用户名
                                UNIQUE (source, name)                    -- 同一来源的同一用户名只能关联一次
);

-- 创建organization表（存储组织信息）
CREATE TABLE organization (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 组织ID，自增主键
                              uuid VARCHAR(36) NOT NULL UNIQUE,         -- 组织全局唯一标识符
                              name VARCHAR(255) NOT NULL UNIQUE,       -- 组织名称，不可为空且唯一
                              status VARCHAR(50) NOT NULL,             -- 组织状态（如ACTIVE, PENDING等）
                              date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 组织创建日期，默认为当前时间
                              avatar VARCHAR(255),                     -- 组织头像路径
                              description TEXT,                        -- 组织描述
                              rating BIGINT DEFAULT 0,                 -- 组织评分
                              free_text TEXT,                          -- 组织自由描述文本（根据需求添加）
                              is_verified TINYINT(1) NOT NULL DEFAULT 0, -- 是否已认证（管理员审核）
                              reject_reason TEXT,                      -- 审核拒绝原因
                              review_date TIMESTAMP NULL,              -- 审核日期
                              reviewed_by BIGINT,                      -- 审核者ID
                              -- 可见性与策略
                              is_public TINYINT(1) NOT NULL DEFAULT 1,            -- 是否公开可见
                              allow_join_request TINYINT(1) NOT NULL DEFAULT 0,  -- 是否允许申请加入
                              allow_invite_link TINYINT(1) NOT NULL DEFAULT 1    -- 是否允许生成邀请链接
);

-- 创建lnk_user_organization表（用户与组织的关联关系）
CREATE TABLE lnk_user_organization (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                       uuid VARCHAR(36) NOT NULL UNIQUE,         -- 关联关系全局唯一标识符
                                       organization_id BIGINT NOT NULL,         -- 组织ID，逻辑外键 -> organization(id)
                                       user_id BIGINT NOT NULL,                 -- 用户ID，逻辑外键 -> user(id)
                                       role VARCHAR(50) NOT NULL,               -- 用户在组织中的角色（如ADMIN, MEMBER等）
                                       UNIQUE (organization_id, user_id)        -- 一个用户在一个组织中只能有一个角色
);

-- 创建raw_cosv_file表（存储原始COSV文件信息）
CREATE TABLE raw_cosv_file (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                               uuid VARCHAR(36) NOT NULL UNIQUE,         -- 文件全局唯一标识符
                               file_name VARCHAR(255) NOT NULL,         -- 文件名称
                               user_id BIGINT NOT NULL,                 -- 上传用户ID，逻辑外键 -> user(id)
                               organization_id BIGINT NOT NULL,         -- 所属组织ID，逻辑外键 -> organization(id)
                               status VARCHAR(50) NOT NULL,             -- 文件状态（如UPLOADED, PROCESSED等）
                               status_message TEXT,                     -- 状态消息（如错误信息）
                               content_length BIGINT,                   -- 文件内容长度
                               create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建日期，默认为当前时间
                               update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 更新日期，自动更新
                               -- 原文存储/追溯增强
                               storage_url VARCHAR(1024) NULL,          -- 原文对象存储地址（可选）
                               content LONGBLOB NULL,                   -- 原文内容（可选，存一项即可）
                               checksum_sha256 CHAR(64) NULL,           -- 原文校验值
                               mime_type VARCHAR(128) NULL              -- 原文MIME类型
);

-- 创建cosv_file表（存储处理后的COSV文件信息）
CREATE TABLE cosv_file (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                           uuid VARCHAR(36) NOT NULL UNIQUE,         -- 文件全局唯一标识符
                           identifier VARCHAR(255) NOT NULL,        -- COSV文件标识符
                           modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 修改时间，默认为当前时间
                           prev_cosv_file_id BIGINT,                -- 前一个版本的COSV文件ID，逻辑外键 -> cosv_file(id)
                           user_id BIGINT,                          -- 创建/更新文件的用户ID，逻辑外键 -> user(id)
                           schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0', -- 文件对应Schema版本
                           raw_cosv_file_id BIGINT NULL             -- 原始文件ID（可为空），逻辑外键 -> raw_cosv_file(id)
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
                                        uuid VARCHAR(36) NOT NULL UNIQUE,         -- 漏洞全局唯一标识符
                                        identifier VARCHAR(255) NOT NULL UNIQUE, -- 漏洞标识符（如COSV-2023-1234），唯一
                                        summary VARCHAR(255) NOT NULL,           -- 漏洞摘要
                                        details TEXT NOT NULL,                   -- 漏洞详情
                                        severity_num FLOAT NOT NULL,             -- 严重性评分
                                        modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 修改时间，默认为当前时间
                                        submitted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 提交时间，默认为当前时间
                                        published TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,    -- 条目发布日期（UTC）
                                        withdrawn TIMESTAMP NULL,             -- 条目撤回日期（UTC）
                                        language VARCHAR(50) NOT NULL,           -- 编程语言
                                        status VARCHAR(50) NOT NULL,             -- 漏洞状态
                                        user_id BIGINT NOT NULL,                 -- 提交用户ID，逻辑外键 -> user(id)
                                        organization_id BIGINT,                  -- 关联组织ID，逻辑外键 -> organization(id)
                                        category_id BIGINT,                      -- 分类ID，逻辑外键 -> category(id)
                                        latest_cosv_file_id BIGINT NOT NULL,     -- 最新COSV文件ID，逻辑外键 -> cosv_file(id)
                                        schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0', -- COSV Schema版本
                                        review_date TIMESTAMP NULL,              -- 审核日期
                                        reviewed_by BIGINT NULL,                 -- 审核者ID
                                        reject_reason TEXT NULL,                 -- 拒绝原因
                                        confirmed_type VARCHAR(32) NULL,         -- 确认方式/状态
                                        database_specific JSON NULL              -- 数据库特定扩展（JSON）
);

-- 创建vulnerability_metadata_project表（存储漏洞相关项目信息）
CREATE TABLE vulnerability_metadata_project (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                                                uuid VARCHAR(36) NOT NULL UNIQUE,         -- 项目全局唯一标识符
                                                name VARCHAR(255) NOT NULL,              -- 项目名称
                                                url VARCHAR(255) NOT NULL,               -- 项目URL
                                                versions TEXT NOT NULL,                  -- 受影响的版本
                                                vulnerability_metadata_id BIGINT NOT NULL, -- 关联的漏洞元数据ID，逻辑外键 -> vulnerability_metadata(id)
                                                type VARCHAR(50) NOT NULL                -- 关联类型
);

-- 创建tag表（存储标签信息）
CREATE TABLE tag (
                     id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 自增主键
                     uuid VARCHAR(36) NOT NULL UNIQUE,         -- 标签全局唯一标识符
                     code VARCHAR(64) NOT NULL UNIQUE,         -- 标签稳定代码
                     name VARCHAR(255) NOT NULL UNIQUE,        -- 标签名称（展示），当前保持唯一
                     create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建日期，默认为当前时间
);

-- 创建category表（存储分类信息）
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,         -- 自增主键
    uuid VARCHAR(36) NOT NULL UNIQUE,             -- 分类全局唯一标识符
    code VARCHAR(64) NOT NULL UNIQUE,             -- 分类代码，稳定键
    name VARCHAR(255) NOT NULL,                   -- 分类展示名
    description TEXT NULL,                        -- 分类描述
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间
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
CREATE INDEX idx_user_uuid ON user(uuid);                       -- 用户UUID索引
CREATE UNIQUE INDEX idx_user_email ON user(email);              -- 用户邮箱唯一索引

CREATE INDEX idx_organization_name ON organization(name);       -- 组织名索引
CREATE INDEX idx_organization_status ON organization(status);   -- 组织状态索引
CREATE INDEX idx_organization_uuid ON organization(uuid);       -- 组织UUID索引

-- 逻辑外键索引
CREATE INDEX idx_original_login_user_id ON original_login(user_id); -- 用户登录-用户索引
CREATE INDEX idx_original_login_uuid ON original_login(uuid);     -- 登录信息UUID索引

CREATE INDEX idx_lnk_user_organization_user_id ON lnk_user_organization(user_id); -- 用户-组织关联用户索引
CREATE INDEX idx_lnk_user_organization_organization_id ON lnk_user_organization(organization_id); -- 用户-组织关联组织索引

-- 系统设置（键值对）
CREATE TABLE IF NOT EXISTS app_setting (
    `key` VARCHAR(128) PRIMARY KEY,
    `value` TEXT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_lnk_user_organization_role ON lnk_user_organization(role); -- 角色索引
CREATE INDEX idx_lnk_user_organization_compound ON lnk_user_organization(user_id, organization_id); -- 用户-组织组合索引
CREATE INDEX idx_lnk_user_organization_uuid ON lnk_user_organization(uuid); -- 用户-组织关联UUID索引

CREATE INDEX idx_raw_cosv_file_user_id ON raw_cosv_file(user_id);     -- 原始文件-用户索引
CREATE INDEX idx_raw_cosv_file_status ON raw_cosv_file(status);       -- 原始文件状态索引
CREATE INDEX idx_raw_cosv_file_organization_id ON raw_cosv_file(organization_id); -- 原始文件-组织索引
CREATE INDEX idx_raw_cosv_file_uuid ON raw_cosv_file(uuid);           -- 原始文件UUID索引

CREATE INDEX idx_cosv_file_identifier ON cosv_file(identifier);       -- COSV文件标识符索引
CREATE INDEX idx_cosv_file_user_id ON cosv_file(user_id);             -- COSV文件-用户索引
CREATE INDEX idx_cosv_file_prev_file_id ON cosv_file(prev_cosv_file_id); -- COSV文件-前一版本索引
CREATE INDEX idx_cosv_file_uuid ON cosv_file(uuid);                   -- COSV文件UUID索引

CREATE INDEX idx_vulnerability_metadata_identifier ON vulnerability_metadata(identifier); -- 漏洞标识符索引
CREATE INDEX idx_vulnerability_metadata_language ON vulnerability_metadata(language);     -- 漏洞语言索引
CREATE INDEX idx_vulnerability_metadata_status ON vulnerability_metadata(status);         -- 漏洞状态索引
CREATE INDEX idx_vulnerability_metadata_published ON vulnerability_metadata(published);
CREATE INDEX idx_vulnerability_metadata_withdrawn ON vulnerability_metadata(withdrawn);
CREATE INDEX idx_vulnerability_metadata_user_id ON vulnerability_metadata(user_id);       -- 漏洞-用户索引
CREATE INDEX idx_vulnerability_metadata_organization_id ON vulnerability_metadata(organization_id); -- 漏洞-组织索引
CREATE INDEX idx_vulnerability_metadata_category_id ON vulnerability_metadata(category_id); -- 漏洞-分类索引
CREATE INDEX idx_vulnerability_metadata_latest_file_id ON vulnerability_metadata(latest_cosv_file_id); -- 漏洞-最新文件索引
CREATE INDEX idx_vulnerability_metadata_uuid ON vulnerability_metadata(uuid);             -- 漏洞UUID索引

CREATE INDEX idx_vulnerability_metadata_project_type ON vulnerability_metadata_project(type); -- 项目类型索引
CREATE INDEX idx_vulnerability_metadata_project_vuln_id ON vulnerability_metadata_project(vulnerability_metadata_id); -- 项目-漏洞索引
CREATE INDEX idx_vulnerability_metadata_project_uuid ON vulnerability_metadata_project(uuid); -- 项目UUID索引

CREATE INDEX idx_tag_name ON tag(name);                         -- 标签名索引

CREATE INDEX idx_lnk_vulnerability_metadata_tag_vuln_id ON lnk_vulnerability_metadata_tag(vulnerability_metadata_id); -- 漏洞标签-漏洞索引
CREATE INDEX idx_lnk_vulnerability_metadata_tag_tag_id ON lnk_vulnerability_metadata_tag(tag_id); -- 漏洞标签-标签索引
CREATE INDEX idx_lnk_vulnerability_metadata_tag_compound ON lnk_vulnerability_metadata_tag(vulnerability_metadata_id, tag_id); -- 漏洞标签组合索引

CREATE INDEX idx_category_code ON category(code);
CREATE INDEX idx_category_uuid ON category(uuid);
CREATE INDEX idx_category_name ON category(name);
-- 组织的公开+状态组合索引（公开搜索）
CREATE INDEX idx_org_public_status ON organization(is_public, status);

-- ==============================
-- COSV 关联/引用/类型/时间线/危险性建模
-- ==============================

-- 等价ID（aliases）
CREATE TABLE IF NOT EXISTS vulnerability_metadata_alias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    UNIQUE (vulnerability_metadata_id, value)
);
CREATE INDEX idx_vm_alias_vmid ON vulnerability_metadata_alias(vulnerability_metadata_id);

-- 相关ID（related）
CREATE TABLE IF NOT EXISTS vulnerability_metadata_related (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    UNIQUE (vulnerability_metadata_id, value)
);
CREATE INDEX idx_vm_related_vmid ON vulnerability_metadata_related(vulnerability_metadata_id);

-- 外部引用（references）
CREATE TABLE IF NOT EXISTS vulnerability_metadata_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    -- 注意：utf8mb4 下 URL 可能超出 InnoDB 索引长度，使用前缀唯一键
    UNIQUE uniq_vm_ref_vmid_url255 (vulnerability_metadata_id, url(255))
);
CREATE INDEX idx_vm_ref_type ON vulnerability_metadata_reference(type);
CREATE INDEX idx_vm_ref_url ON vulnerability_metadata_reference(url(255));

-- CWE 列表
CREATE TABLE IF NOT EXISTS vulnerability_metadata_cwe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    cwe_id VARCHAR(32) NULL,
    cwe_name VARCHAR(255) NULL
);
CREATE INDEX idx_vm_cwe_vmid ON vulnerability_metadata_cwe(vulnerability_metadata_id);
CREATE INDEX idx_vm_cwe_id ON vulnerability_metadata_cwe(cwe_id);

-- 漏洞本体时间线
CREATE TABLE IF NOT EXISTS vulnerability_metadata_timeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    value TIMESTAMP NOT NULL
);
CREATE INDEX idx_vm_timeline_vmid ON vulnerability_metadata_timeline(vulnerability_metadata_id);
CREATE INDEX idx_vm_timeline_type ON vulnerability_metadata_timeline(type);
CREATE INDEX idx_vm_timeline_value ON vulnerability_metadata_timeline(value);

-- 多量表危险性
CREATE TABLE IF NOT EXISTS vulnerability_metadata_severity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    score VARCHAR(256) NULL,
    level VARCHAR(32) NULL,
    score_num DECIMAL(4,1) NULL
);
CREATE INDEX idx_vm_sev_vmid ON vulnerability_metadata_severity(vulnerability_metadata_id);
CREATE INDEX idx_vm_sev_type ON vulnerability_metadata_severity(type);
CREATE INDEX idx_vm_sev_level ON vulnerability_metadata_severity(level);

-- ==============================
-- 受影响面（affected）完整建模
-- ==============================

-- 受影响包
CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_package (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    ecosystem VARCHAR(64) NULL,
    name VARCHAR(255) NULL,
    purl VARCHAR(512) NULL,
    language VARCHAR(64) NULL,
    repository VARCHAR(1024) NULL,
    home_page VARCHAR(1024) NULL,
    edition VARCHAR(128) NULL,
    ecosystem_specific JSON NULL,
    database_specific JSON NULL
);
CREATE INDEX idx_vm_pkg_vmid ON vulnerability_metadata_affected_package(vulnerability_metadata_id);
CREATE INDEX idx_vm_pkg_purl ON vulnerability_metadata_affected_package(purl(255));
CREATE INDEX idx_vm_pkg_eco ON vulnerability_metadata_affected_package(ecosystem);
CREATE INDEX idx_vm_pkg_name ON vulnerability_metadata_affected_package(name);

-- 引入/修复提交
CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_commit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    commit_type VARCHAR(16) NOT NULL, -- INTRODUCED / FIXED
    commit_id VARCHAR(64) NOT NULL
);
CREATE INDEX idx_vm_pkg_commit_pid ON vulnerability_metadata_affected_commit(package_id);
CREATE INDEX idx_vm_pkg_commit_type ON vulnerability_metadata_affected_commit(commit_type);

-- 版本范围
CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL, -- ECOSYSTEM / SEMVER / GIT
    repo VARCHAR(1024) NULL,
    database_specific JSON NULL
);
CREATE INDEX idx_vm_range_pid ON vulnerability_metadata_affected_range(package_id);
CREATE INDEX idx_vm_range_type ON vulnerability_metadata_affected_range(type);

-- 版本范围事件端点
CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    range_id BIGINT NOT NULL,
    event_type VARCHAR(16) NOT NULL, -- introduced / fixed / last_affected / limit
    value VARCHAR(128) NOT NULL,
    UNIQUE (range_id, event_type, value)
);
CREATE INDEX idx_vm_range_event_rid ON vulnerability_metadata_affected_range_event(range_id);
CREATE INDEX idx_vm_range_event_type ON vulnerability_metadata_affected_range_event(event_type);

-- 受影响版本枚举
CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    version VARCHAR(128) NOT NULL,
    UNIQUE (package_id, version)
);
CREATE INDEX idx_vm_version_pid ON vulnerability_metadata_affected_version(package_id);

-- ==============================
-- 补丁/贡献/致谢/利用状态
-- ==============================

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    patch_url VARCHAR(1024) NULL,
    issue_url VARCHAR(1024) NULL,
    main_language VARCHAR(64) NULL,
    author VARCHAR(255) NULL,
    committer VARCHAR(255) NULL
);
CREATE INDEX idx_vm_patch_vmid ON vulnerability_metadata_patch_detail(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_branch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patch_detail_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL
);
CREATE INDEX idx_vm_patch_branch_pid ON vulnerability_metadata_patch_branch(patch_detail_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patch_detail_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL
);
CREATE INDEX idx_vm_patch_tag_pid ON vulnerability_metadata_patch_tag(patch_detail_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_contributor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    org VARCHAR(255) NULL,
    name VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    contributions VARCHAR(1024) NULL
);
CREATE INDEX idx_vm_contrib_vmid ON vulnerability_metadata_contributor(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(64) NULL
);
CREATE INDEX idx_vm_credit_vmid ON vulnerability_metadata_credit(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit_contact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    credit_id BIGINT NOT NULL,
    contact VARCHAR(1024) NOT NULL
);
CREATE INDEX idx_vm_credit_contact_cid ON vulnerability_metadata_credit_contact(credit_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_exploit_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vulnerability_metadata_id BIGINT NOT NULL,
    status VARCHAR(64) NOT NULL
);
CREATE INDEX idx_vm_exploit_status_vmid ON vulnerability_metadata_exploit_status(vulnerability_metadata_id);


CREATE INDEX idx_lnk_vulnerability_metadata_user_vuln_id ON lnk_vulnerability_metadata_user(vulnerability_metadata_id); -- 漏洞用户-漏洞索引
CREATE INDEX idx_lnk_vulnerability_metadata_user_user_id ON lnk_vulnerability_metadata_user(user_id); -- 漏洞用户-用户索引
CREATE INDEX idx_lnk_vulnerability_metadata_user_compound ON lnk_vulnerability_metadata_user(vulnerability_metadata_id, user_id); -- 漏洞用户组合索引

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,                -- 通知全局唯一标识符
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
CREATE INDEX idx_notification_user_id ON notification(user_id);
CREATE INDEX idx_notification_type ON notification(type);
CREATE INDEX idx_notification_target_id ON notification(target_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_create_time ON notification(create_time);
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_uuid ON notification(uuid);

-- API密钥表
CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT 'API密钥的唯一公共标识符',
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
CREATE INDEX idx_apikey_prefix ON api_key(key_prefix);
CREATE INDEX idx_apikey_creator ON api_key(creator_user_id);
CREATE INDEX idx_apikey_organization ON api_key(organization_id);
CREATE INDEX idx_apikey_status ON api_key(status);
CREATE INDEX idx_apikey_expire_time ON api_key(expire_time);
CREATE INDEX idx_apikey_uuid ON api_key(uuid);

-- API密钥使用日志表
CREATE TABLE IF NOT EXISTS api_key_usage_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE COMMENT '日志记录的唯一标识符',
    api_key_id BIGINT NOT NULL COMMENT '关联的API密钥ID',
    request_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间戳',
    request_ip_address VARCHAR(45) NOT NULL COMMENT '请求来源IP地址',
    request_method VARCHAR(10) NOT NULL COMMENT 'HTTP请求方法 (GET, POST, etc.)',
    request_path VARCHAR(512) NOT NULL COMMENT '请求的API路径',
    response_status_code INT NOT NULL COMMENT 'HTTP响应状态码',
    user_agent TEXT COMMENT '请求的User-Agent头',
    INDEX idx_log_api_key_id (api_key_id),
    INDEX idx_log_request_timestamp (request_timestamp),
    INDEX idx_log_uuid (uuid)
) COMMENT='记录每一次API密钥的使用详情';

-- 组织邀请链接表（用于邀请码加入组织）
CREATE TABLE IF NOT EXISTS org_invite_link (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  uuid VARCHAR(64) NOT NULL UNIQUE,
  org_id BIGINT NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  created_by BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT NOW(),
  expire_time DATETIME NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  INDEX idx_org (org_id),
  INDEX idx_active_expire (is_active, expire_time)
);

-- 用户积分流水表
CREATE TABLE IF NOT EXISTS user_points_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    delta INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    ref_type VARCHAR(50) NULL,
    ref_id VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='用户积分流水表';

CREATE UNIQUE INDEX uniq_user_points_idem ON user_points_ledger(user_id, idempotency_key);
CREATE INDEX idx_user_points_user ON user_points_ledger(user_id);
CREATE INDEX idx_user_points_created ON user_points_ledger(created_at);

-- 组织积分流水表
CREATE TABLE IF NOT EXISTS org_points_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL,
    delta INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    ref_type VARCHAR(50) NULL,
    ref_id VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='组织积分流水表';

CREATE UNIQUE INDEX uniq_org_points_idem ON org_points_ledger(organization_id, idempotency_key);
CREATE INDEX idx_org_points_org ON org_points_ledger(organization_id);
CREATE INDEX idx_org_points_created ON org_points_ledger(created_at);
