-- Safe migration patch to align existing DB with current model
-- Avoids AFTER clauses; uses IF NOT EXISTS where supported (MySQL 8.0+)

USE cosv_horizon;

-- 1) Extend vulnerability_metadata columns
ALTER TABLE vulnerability_metadata
  ADD COLUMN IF NOT EXISTS published TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN IF NOT EXISTS withdrawn TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS language VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS status VARCHAR(50) NULL,
  ADD COLUMN IF NOT EXISTS category_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
  ADD COLUMN IF NOT EXISTS review_date TIMESTAMP NULL,
  ADD COLUMN IF NOT EXISTS reviewed_by BIGINT NULL,
  ADD COLUMN IF NOT EXISTS reject_reason TEXT NULL,
  ADD COLUMN IF NOT EXISTS confirmed_type VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS database_specific JSON NULL;

-- 2) Extend cosv_file
ALTER TABLE cosv_file
  ADD COLUMN IF NOT EXISTS schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
  ADD COLUMN IF NOT EXISTS raw_cosv_file_id BIGINT NULL;

-- 3) Extend raw_cosv_file
ALTER TABLE raw_cosv_file
  ADD COLUMN IF NOT EXISTS storage_url VARCHAR(1024) NULL,
  ADD COLUMN IF NOT EXISTS content LONGBLOB NULL,
  ADD COLUMN IF NOT EXISTS checksum_sha256 CHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS mime_type VARCHAR(128) NULL;

-- 4) COSV extension tables
CREATE TABLE IF NOT EXISTS vulnerability_metadata_alias (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  UNIQUE (vulnerability_metadata_id, value)
);
CREATE INDEX IF NOT EXISTS idx_vm_alias_vmid ON vulnerability_metadata_alias(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_related (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  value VARCHAR(255) NOT NULL,
  UNIQUE (vulnerability_metadata_id, value)
);
CREATE INDEX IF NOT EXISTS idx_vm_related_vmid ON vulnerability_metadata_related(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_reference (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  url VARCHAR(1024) NOT NULL,
  UNIQUE (vulnerability_metadata_id, url)
);
CREATE INDEX IF NOT EXISTS idx_vm_ref_type ON vulnerability_metadata_reference(type);
CREATE INDEX IF NOT EXISTS idx_vm_ref_url ON vulnerability_metadata_reference(url(255));

CREATE TABLE IF NOT EXISTS vulnerability_metadata_cwe (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  cwe_id VARCHAR(32) NULL,
  cwe_name VARCHAR(255) NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_cwe_vmid ON vulnerability_metadata_cwe(vulnerability_metadata_id);
CREATE INDEX IF NOT EXISTS idx_vm_cwe_id ON vulnerability_metadata_cwe(cwe_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_timeline (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  value TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_timeline_vmid ON vulnerability_metadata_timeline(vulnerability_metadata_id);
CREATE INDEX IF NOT EXISTS idx_vm_timeline_type ON vulnerability_metadata_timeline(type);
CREATE INDEX IF NOT EXISTS idx_vm_timeline_value ON vulnerability_metadata_timeline(value);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_severity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  score VARCHAR(256) NULL,
  level VARCHAR(32) NULL,
  score_num DECIMAL(4,1) NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_sev_vmid ON vulnerability_metadata_severity(vulnerability_metadata_id);
CREATE INDEX IF NOT EXISTS idx_vm_sev_type ON vulnerability_metadata_severity(type);
CREATE INDEX IF NOT EXISTS idx_vm_sev_level ON vulnerability_metadata_severity(level);

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
CREATE INDEX IF NOT EXISTS idx_vm_pkg_vmid ON vulnerability_metadata_affected_package(vulnerability_metadata_id);
CREATE INDEX IF NOT EXISTS idx_vm_pkg_purl ON vulnerability_metadata_affected_package(purl(255));
CREATE INDEX IF NOT EXISTS idx_vm_pkg_eco ON vulnerability_metadata_affected_package(ecosystem);
CREATE INDEX IF NOT EXISTS idx_vm_pkg_name ON vulnerability_metadata_affected_package(name);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_commit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  commit_type VARCHAR(16) NOT NULL,
  commit_id VARCHAR(64) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_pkg_commit_pid ON vulnerability_metadata_affected_commit(package_id);
CREATE INDEX IF NOT EXISTS idx_vm_pkg_commit_type ON vulnerability_metadata_affected_commit(commit_type);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  type VARCHAR(16) NOT NULL,
  repo VARCHAR(1024) NULL,
  database_specific JSON NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_range_pid ON vulnerability_metadata_affected_range(package_id);
CREATE INDEX IF NOT EXISTS idx_vm_range_type ON vulnerability_metadata_affected_range(type);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_range_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  range_id BIGINT NOT NULL,
  event_type VARCHAR(16) NOT NULL,
  value VARCHAR(128) NOT NULL,
  UNIQUE (range_id, event_type, value)
);
CREATE INDEX IF NOT EXISTS idx_vm_range_event_rid ON vulnerability_metadata_affected_range_event(range_id);
CREATE INDEX IF NOT EXISTS idx_vm_range_event_type ON vulnerability_metadata_affected_range_event(event_type);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_affected_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_id BIGINT NOT NULL,
  version VARCHAR(128) NOT NULL,
  UNIQUE (package_id, version)
);
CREATE INDEX IF NOT EXISTS idx_vm_version_pid ON vulnerability_metadata_affected_version(package_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  patch_url VARCHAR(1024) NULL,
  issue_url VARCHAR(1024) NULL,
  main_language VARCHAR(64) NULL,
  author VARCHAR(255) NULL,
  committer VARCHAR(255) NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_patch_vmid ON vulnerability_metadata_patch_detail(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_branch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patch_detail_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_patch_branch_pid ON vulnerability_metadata_patch_branch(patch_detail_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_patch_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patch_detail_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_patch_tag_pid ON vulnerability_metadata_patch_tag(patch_detail_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_contributor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  org VARCHAR(255) NULL,
  name VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  contributions VARCHAR(1024) NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_contrib_vmid ON vulnerability_metadata_contributor(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(64) NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_credit_vmid ON vulnerability_metadata_credit(vulnerability_metadata_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_credit_contact (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  credit_id BIGINT NOT NULL,
  contact VARCHAR(1024) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_credit_contact_cid ON vulnerability_metadata_credit_contact(credit_id);

CREATE TABLE IF NOT EXISTS vulnerability_metadata_exploit_status (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vulnerability_metadata_id BIGINT NOT NULL,
  status VARCHAR(64) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_exploit_status_vmid ON vulnerability_metadata_exploit_status(vulnerability_metadata_id);

-- 5) Helpful indexes
CREATE INDEX IF NOT EXISTS idx_vulnerability_metadata_published ON vulnerability_metadata(published);
CREATE INDEX IF NOT EXISTS idx_vulnerability_metadata_withdrawn ON vulnerability_metadata(withdrawn);

