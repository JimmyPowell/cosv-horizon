-- Safe migration for MySQL (without ADD COLUMN IF NOT EXISTS)
-- Uses information_schema checks + dynamic SQL

SET @db := DATABASE();
USE cosv_horizon;

-- Helper: add column if missing
-- call by setting @tbl, @col, @ddl then executing the block
-- Example:
--   SET @tbl='vulnerability_metadata'; SET @col='published';
--   SET @ddl='ALTER TABLE vulnerability_metadata ADD COLUMN published TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP';
--   <run block>
DELIMITER $$
DROP PROCEDURE IF EXISTS add_column_if_missing $$
CREATE PROCEDURE add_column_if_missing(IN dbName VARCHAR(64), IN tblName VARCHAR(64), IN colName VARCHAR(64), IN addDdl TEXT)
BEGIN
  DECLARE cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO cnt
    FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tblName AND COLUMN_NAME = colName;
  IF cnt = 0 THEN
    SET @sql := addDdl;
    PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
  END IF;
END $$
DELIMITER ;

-- Helper: add index if missing (by name)
DELIMITER $$
DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(IN dbName VARCHAR(64), IN tblName VARCHAR(64), IN idxName VARCHAR(128), IN addDdl TEXT)
BEGIN
  DECLARE cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO cnt
    FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = dbName AND TABLE_NAME = tblName AND INDEX_NAME = idxName;
  IF cnt = 0 THEN
    SET @sql := addDdl;
    PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
  END IF;
END $$
DELIMITER ;

-- 1) Columns on vulnerability_metadata
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'published', 'ALTER TABLE vulnerability_metadata ADD COLUMN published TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'withdrawn', 'ALTER TABLE vulnerability_metadata ADD COLUMN withdrawn TIMESTAMP NULL');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'schema_version', 'ALTER TABLE vulnerability_metadata ADD COLUMN schema_version VARCHAR(16) NOT NULL DEFAULT \'1.0.0\'');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'review_date', 'ALTER TABLE vulnerability_metadata ADD COLUMN review_date TIMESTAMP NULL');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'reviewed_by', 'ALTER TABLE vulnerability_metadata ADD COLUMN reviewed_by BIGINT NULL');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'reject_reason', 'ALTER TABLE vulnerability_metadata ADD COLUMN reject_reason TEXT NULL');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'confirmed_type', 'ALTER TABLE vulnerability_metadata ADD COLUMN confirmed_type VARCHAR(32) NULL');
CALL add_column_if_missing(@db, 'vulnerability_metadata', 'database_specific', 'ALTER TABLE vulnerability_metadata ADD COLUMN database_specific JSON NULL');

-- Helpful indexes on vulnerability_metadata
CALL add_index_if_missing(@db, 'vulnerability_metadata', 'idx_vulnerability_metadata_published', 'CREATE INDEX idx_vulnerability_metadata_published ON vulnerability_metadata(published)');
CALL add_index_if_missing(@db, 'vulnerability_metadata', 'idx_vulnerability_metadata_withdrawn', 'CREATE INDEX idx_vulnerability_metadata_withdrawn ON vulnerability_metadata(withdrawn)');

-- 2) Columns on cosv_file
CALL add_column_if_missing(@db, 'cosv_file', 'schema_version', 'ALTER TABLE cosv_file ADD COLUMN schema_version VARCHAR(16) NOT NULL DEFAULT \'1.0.0\'');
CALL add_column_if_missing(@db, 'cosv_file', 'raw_cosv_file_id', 'ALTER TABLE cosv_file ADD COLUMN raw_cosv_file_id BIGINT NULL');

-- 3) Columns on raw_cosv_file
CALL add_column_if_missing(@db, 'raw_cosv_file', 'storage_url', 'ALTER TABLE raw_cosv_file ADD COLUMN storage_url VARCHAR(1024) NULL');
CALL add_column_if_missing(@db, 'raw_cosv_file', 'content', 'ALTER TABLE raw_cosv_file ADD COLUMN content LONGBLOB NULL');
CALL add_column_if_missing(@db, 'raw_cosv_file', 'checksum_sha256', 'ALTER TABLE raw_cosv_file ADD COLUMN checksum_sha256 CHAR(64) NULL');
CALL add_column_if_missing(@db, 'raw_cosv_file', 'mime_type', 'ALTER TABLE raw_cosv_file ADD COLUMN mime_type VARCHAR(128) NULL');

-- 4) Create COSV extension tables if absent
CREATE TABLE IF NOT EXISTS vulnerability_metadata_alias (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  UNIQUE (vulnerability_metadata_id, value)
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_alias', 'idx_vm_alias_vmid', 'CREATE INDEX idx_vm_alias_vmid ON vulnerability_metadata_alias(vulnerability_metadata_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_related (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  UNIQUE (vulnerability_metadata_id, value)
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_related', 'idx_vm_related_vmid', 'CREATE INDEX idx_vm_related_vmid ON vulnerability_metadata_related(vulnerability_metadata_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_reference (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  url VARCHAR(1024) NOT NULL,
  UNIQUE (vulnerability_metadata_id, url)
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_reference', 'idx_vm_ref_type', 'CREATE INDEX idx_vm_ref_type ON vulnerability_metadata_reference(type)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_reference', 'idx_vm_ref_url', 'CREATE INDEX idx_vm_ref_url ON vulnerability_metadata_reference(url(255))');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_cwe (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  cwe_id VARCHAR(32) NULL,
  cwe_name VARCHAR(255) NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_cwe', 'idx_vm_cwe_vmid', 'CREATE INDEX idx_vm_cwe_vmid ON vulnerability_metadata_cwe(vulnerability_metadata_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_cwe', 'idx_vm_cwe_id', 'CREATE INDEX idx_vm_cwe_id ON vulnerability_metadata_cwe(cwe_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_timeline (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  value TIMESTAMP NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_timeline', 'idx_vm_timeline_vmid', 'CREATE INDEX idx_vm_timeline_vmid ON vulnerability_metadata_timeline(vulnerability_metadata_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_timeline', 'idx_vm_timeline_type', 'CREATE INDEX idx_vm_timeline_type ON vulnerability_metadata_timeline(type)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_timeline', 'idx_vm_timeline_value', 'CREATE INDEX idx_vm_timeline_value ON vulnerability_metadata_timeline(value)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_severity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  score VARCHAR(256) NULL,
  level VARCHAR(32) NULL,
  score_num DECIMAL(4,1) NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_severity', 'idx_vm_sev_vmid', 'CREATE INDEX idx_vm_sev_vmid ON vulnerability_metadata_severity(vulnerability_metadata_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_severity', 'idx_vm_sev_type', 'CREATE INDEX idx_vm_sev_type ON vulnerability_metadata_severity(type)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_severity', 'idx_vm_sev_level', 'CREATE INDEX idx_vm_sev_level ON vulnerability_metadata_severity(level)');

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
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_package', 'idx_vm_pkg_vmid', 'CREATE INDEX idx_vm_pkg_vmid ON vulnerability_metadata_affected_package(vulnerability_metadata_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_package', 'idx_vm_pkg_purl', 'CREATE INDEX idx_vm_pkg_purl ON vulnerability_metadata_affected_package(purl(255))');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_package', 'idx_vm_pkg_eco', 'CREATE INDEX idx_vm_pkg_eco ON vulnerability_metadata_affected_package(ecosystem)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_package', 'idx_vm_pkg_name', 'CREATE INDEX idx_vm_pkg_name ON vulnerability_metadata_affected_package(name)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_commit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  commit_type VARCHAR(16) NOT NULL,
  commit_id VARCHAR(64) NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_commit', 'idx_vm_pkg_commit_pid', 'CREATE INDEX idx_vm_pkg_commit_pid ON vulnerability_metadata_affected_commit(package_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_commit', 'idx_vm_pkg_commit_type', 'CREATE INDEX idx_vm_pkg_commit_type ON vulnerability_metadata_affected_commit(commit_type)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  type VARCHAR(16) NOT NULL,
  repo VARCHAR(1024) NULL,
  database_specific JSON NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_range', 'idx_vm_range_pid', 'CREATE INDEX idx_vm_range_pid ON vulnerability_metadata_affected_range(package_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_range', 'idx_vm_range_type', 'CREATE INDEX idx_vm_range_type ON vulnerability_metadata_affected_range(type)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  range_id BIGINT NOT NULL,
  event_type VARCHAR(16) NOT NULL,
  value VARCHAR(128) NOT NULL,
  UNIQUE (range_id, event_type, value)
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_range_event', 'idx_vm_range_event_rid', 'CREATE INDEX idx_vm_range_event_rid ON vulnerability_metadata_affected_range_event(range_id)');
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_range_event', 'idx_vm_range_event_type', 'CREATE INDEX idx_vm_range_event_type ON vulnerability_metadata_affected_range_event(event_type)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  version VARCHAR(128) NOT NULL,
  UNIQUE (package_id, version)
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_affected_version', 'idx_vm_version_pid', 'CREATE INDEX idx_vm_version_pid ON vulnerability_metadata_affected_version(package_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  patch_url VARCHAR(1024) NULL,
  issue_url VARCHAR(1024) NULL,
  main_language VARCHAR(64) NULL,
  author VARCHAR(255) NULL,
  committer VARCHAR(255) NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_patch_detail', 'idx_vm_patch_vmid', 'CREATE INDEX idx_vm_patch_vmid ON vulnerability_metadata_patch_detail(vulnerability_metadata_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_branch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patch_detail_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_patch_branch', 'idx_vm_patch_branch_pid', 'CREATE INDEX idx_vm_patch_branch_pid ON vulnerability_metadata_patch_branch(patch_detail_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patch_detail_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_patch_tag', 'idx_vm_patch_tag_pid', 'CREATE INDEX idx_vm_patch_tag_pid ON vulnerability_metadata_patch_tag(patch_detail_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_contributor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  org VARCHAR(255) NULL,
  name VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  contributions VARCHAR(1024) NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_contributor', 'idx_vm_contrib_vmid', 'CREATE INDEX idx_vm_contrib_vmid ON vulnerability_metadata_contributor(vulnerability_metadata_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(64) NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_credit', 'idx_vm_credit_vmid', 'CREATE INDEX idx_vm_credit_vmid ON vulnerability_metadata_credit(vulnerability_metadata_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit_contact (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  credit_id BIGINT NOT NULL,
  contact VARCHAR(1024) NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_credit_contact', 'idx_vm_credit_contact_cid', 'CREATE INDEX idx_vm_credit_contact_cid ON vulnerability_metadata_credit_contact(credit_id)');

CREATE TABLE IF NOT EXISTS vulnerability_metadata_exploit_status (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  status VARCHAR(64) NOT NULL
);
CALL add_index_if_missing(@db, 'vulnerability_metadata_exploit_status', 'idx_vm_exploit_status_vmid', 'CREATE INDEX idx_vm_exploit_status_vmid ON vulnerability_metadata_exploit_status(vulnerability_metadata_id)');

-- Drop helpers
DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

