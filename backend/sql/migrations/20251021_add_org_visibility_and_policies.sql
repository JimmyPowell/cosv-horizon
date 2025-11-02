-- Migration: add visibility and join/invite policy columns to organization

-- 使用存储过程安全地添加列（如果列不存在）
DELIMITER $$

DROP PROCEDURE IF EXISTS add_org_columns$$

CREATE PROCEDURE add_org_columns()
BEGIN
    -- 检查并添加 is_public 列
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'organization'
        AND COLUMN_NAME = 'is_public'
    ) THEN
        ALTER TABLE organization
        ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织是否公开可见';
    END IF;

    -- 检查并添加 allow_join_request 列
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'organization'
        AND COLUMN_NAME = 'allow_join_request'
    ) THEN
        ALTER TABLE organization
        ADD COLUMN allow_join_request TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许用户申请加入';
    END IF;

    -- 检查并添加 allow_invite_link 列
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'organization'
        AND COLUMN_NAME = 'allow_invite_link'
    ) THEN
        ALTER TABLE organization
        ADD COLUMN allow_invite_link TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过邀请链接加入';
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL add_org_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_org_columns;

