-- Migration: add organization invite link table
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

