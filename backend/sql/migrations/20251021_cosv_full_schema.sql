-- Migration: COSV full schema alignment (aliases/references/CWE/timeline/severity/affected/patch/contributor/credit)
-- Date: 2025-10-21

-- 1) Alter existing tables

-- vulnerability_metadata: add schema_version, published/withdrawn, confirmed_type, database_specific
ALTER TABLE vulnerability_metadata
  ADD COLUMN IF NOT EXISTS published TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP AFTER submitted,
  ADD COLUMN IF NOT EXISTS withdrawn TIMESTAMP NULL AFTER published,
  ADD COLUMN IF NOT EXISTS schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0' AFTER latest_cosv_file_id,
  ADD COLUMN IF NOT EXISTS confirmed_type VARCHAR(32) NULL AFTER reject_reason,
  ADD COLUMN IF NOT EXISTS database_specific JSON NULL AFTER confirmed_type;

-- cosv_file: add schema_version + raw_cosv_file_id
ALTER TABLE cosv_file
  ADD COLUMN IF NOT EXISTS schema_version VARCHAR(16) NOT NULL DEFAULT '1.0.0' AFTER user_id,
  ADD COLUMN IF NOT EXISTS raw_cosv_file_id BIGINT NULL AFTER schema_version;

-- raw_cosv_file: add storage_url/content/checksum/mime_type
ALTER TABLE raw_cosv_file
  ADD COLUMN IF NOT EXISTS storage_url VARCHAR(1024) NULL AFTER update_date,
  ADD COLUMN IF NOT EXISTS content LONGBLOB NULL AFTER storage_url,
  ADD COLUMN IF NOT EXISTS checksum_sha256 CHAR(64) NULL AFTER content,
  ADD COLUMN IF NOT EXISTS mime_type VARCHAR(128) NULL AFTER checksum_sha256;

-- 2) New tables (create if not exists)

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

-- 3) Indexes on new cols
CREATE INDEX IF NOT EXISTS idx_vulnerability_metadata_published ON vulnerability_metadata(published);
CREATE INDEX IF NOT EXISTS idx_vulnerability_metadata_withdrawn ON vulnerability_metadata(withdrawn);

-- 4) Backfill
UPDATE vulnerability_metadata SET schema_version = '1.0.0' WHERE schema_version IS NULL OR schema_version = '';
UPDATE vulnerability_metadata SET published = submitted WHERE published IS NULL;
UPDATE cosv_file SET schema_version = '1.0.0' WHERE schema_version IS NULL OR schema_version = '';

