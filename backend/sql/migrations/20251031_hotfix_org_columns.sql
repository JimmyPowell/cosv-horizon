-- Hotfix: ensure organization table has required columns used by current code
-- - Adds is_verified (primary fix for 500 on /orgs APIs)
-- - Also ensures visibility/policy columns exist (no-op if already present)
--
-- Usage:
--   1) Stop the backend or run during a quiet window to avoid metadata locks
--   2) In MySQL client after selecting the database (USE cosv_horizon), execute this file
--   3) Verify: SHOW COLUMNS FROM organization LIKE 'is_verified';

DELIMITER $$

DROP PROCEDURE IF EXISTS hotfix_org_columns$$

CREATE PROCEDURE hotfix_org_columns()
BEGIN
    -- is_public
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'is_public'
    ) THEN
        ALTER TABLE organization
          ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织是否公开可见';
    END IF;

    -- allow_join_request
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'allow_join_request'
    ) THEN
        ALTER TABLE organization
          ADD COLUMN allow_join_request TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许用户申请加入';
    END IF;

    -- allow_invite_link
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'allow_invite_link'
    ) THEN
        ALTER TABLE organization
          ADD COLUMN allow_invite_link TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过邀请链接加入';
    END IF;

    -- is_verified (main fix)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'is_verified'
    ) THEN
        ALTER TABLE organization
          ADD COLUMN is_verified TINYINT(1) NOT NULL DEFAULT 0 AFTER free_text;
    END IF;
END$$

DELIMITER ;

CALL hotfix_org_columns();
DROP PROCEDURE IF EXISTS hotfix_org_columns;

-- Optional backfill: mark current ACTIVE orgs as verified
-- UPDATE organization SET is_verified = 1 WHERE status = 'ACTIVE' AND is_verified = 0;

