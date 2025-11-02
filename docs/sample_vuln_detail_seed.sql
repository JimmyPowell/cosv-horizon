-- COSV Horizon • Vulnerability detail test data (mock)
-- Purpose: seed a complete vulnerability record for UI preview/testing
-- Note: Designed for MySQL/MariaDB. Run after schema has been created/migrated.

USE cosv_horizon;

START TRANSACTION;

-- 1) User (submitter)
INSERT INTO user (uuid, name, password, role, email, status)
VALUES ('u-mock-0001', 'mockuser', 'password_hash', 'USER', 'mockuser@example.com', 'ACTIVE');
SET @user_id := LAST_INSERT_ID();

-- 2) COSV file (version chain head)
INSERT INTO cosv_file (uuid, identifier, user_id, schema_version)
VALUES ('cf-mock-0001', 'COSV-2025-000123', @user_id, '1.0.0');
SET @cosv_file_id := LAST_INSERT_ID();

-- 3) Category and Tags
INSERT INTO category (uuid, code, name, description)
VALUES ('cat-mock-http-smug', 'HTTP_SMUGGLING', 'HTTP Request Smuggling', 'HTTP 请求走私');
SET @category_id := LAST_INSERT_ID();

INSERT INTO tag (uuid, code, name) VALUES
  ('tag-mock-web', 'WEB', 'Web Security'),
  ('tag-mock-cwe444', 'CWE-444', 'HTTP Request Smuggling');
SET @tag_web_id := (SELECT id FROM tag WHERE code='WEB');
SET @tag_cwe_id := (SELECT id FROM tag WHERE code='CWE-444');

-- 4) Vulnerability metadata (core record)
INSERT INTO vulnerability_metadata (
  uuid, identifier, summary, details, severity_num,
  modified, submitted, published, withdrawn,
  language, status, user_id, organization_id, category_id,
  latest_cosv_file_id, schema_version, review_date, reviewed_by, reject_reason,
  confirmed_type, database_specific
) VALUES (
  'vm-mock-0123',
  'COSV-2025-000123',
  'HTTP 请求走私',
  '此软件包的受影响版本容易受到HTTP请求走私的影响，原因是请求预告片部分解析不正确。\n\n注意：仅当安装了纯Python版本或禁用了扩展时该漏洞可被利用。',
  6.3,
  '2025-07-16 09:00:00',
  '2025-07-15 12:00:00',
  '2025-07-16 09:00:00',
  NULL,
  'PYTHON',
  'ACTIVE',
  @user_id,
  NULL,
  @category_id,
  @cosv_file_id,
  '1.0.0',
  NULL,
  NULL,
  NULL,
  'manual_confirmed',
  NULL
);
SET @vm_id := LAST_INSERT_ID();

-- 5) Link tags
INSERT INTO lnk_vulnerability_metadata_tag (vulnerability_metadata_id, tag_id)
VALUES (@vm_id, @tag_web_id), (@vm_id, @tag_cwe_id);

-- 6) COSV: aliases/related/references/CWE/timeline/severity
INSERT INTO vulnerability_metadata_alias (vulnerability_metadata_id, value)
VALUES (@vm_id, 'CVE-2025-53643');

-- (related left empty in this sample)

INSERT INTO vulnerability_metadata_reference (vulnerability_metadata_id, type, url) VALUES
  (@vm_id, 'advisory', 'https://nvd.nist.gov/vuln/detail/CVE-2025-53643'),
  (@vm_id, 'patch',    'https://github.com/aio-libs/aiohttp/commit/xxxxx');

INSERT INTO vulnerability_metadata_cwe (vulnerability_metadata_id, cwe_id, cwe_name)
VALUES (@vm_id, 'CWE-444', 'HTTP Request Smuggling');

INSERT INTO vulnerability_metadata_timeline (vulnerability_metadata_id, type, value) VALUES
  (@vm_id, 'disclosed', '2025-07-15 00:00:00'),
  (@vm_id, 'fixed',     '2025-07-16 00:00:00');

INSERT INTO vulnerability_metadata_severity (vulnerability_metadata_id, type, score, level, score_num) VALUES
  (@vm_id, 'CVSS:3.1/BASE', 'AV:N/AC:L/...', 'HIGH',   7.5),
  (@vm_id, 'CVSS:4.0',      '.../...',       'MEDIUM', 6.3);

-- 7) COSV: affected (package + ranges + versions)
INSERT INTO vulnerability_metadata_affected_package (
  vulnerability_metadata_id, ecosystem, name, purl, language, repository, home_page, edition, ecosystem_specific, database_specific
) VALUES (
  @vm_id, 'PyPI', 'aiohttp', 'pkg:pypi/aiohttp', 'PYTHON',
  'https://github.com/aio-libs/aiohttp', 'https://pypi.org/project/aiohttp', NULL, NULL, NULL
);
SET @pkg_id := LAST_INSERT_ID();

INSERT INTO vulnerability_metadata_affected_range (package_id, type, repo, database_specific)
VALUES (@pkg_id, 'SEMVER', NULL, NULL);
SET @range_id := LAST_INSERT_ID();

INSERT INTO vulnerability_metadata_affected_range_event (range_id, event_type, value) VALUES
  (@range_id, 'introduced', '3.12.0'),
  (@range_id, 'fixed',      '3.12.14');

INSERT INTO vulnerability_metadata_affected_version (package_id, version) VALUES
  (@pkg_id, '3.12.0'),
  (@pkg_id, '3.12.1'),
  (@pkg_id, '3.12.2');

-- 8) COSV: patch details + branches/tags
INSERT INTO vulnerability_metadata_patch_detail (
  vulnerability_metadata_id, patch_url, issue_url, main_language, author, committer
) VALUES (
  @vm_id, 'https://github.com/aio-libs/aiohttp/commit/xxxxx', 'https://github.com/aio-libs/aiohttp/issues/123', 'Python', 'Alice', 'Bob'
);
SET @pd_id := LAST_INSERT_ID();

INSERT INTO vulnerability_metadata_patch_branch (patch_detail_id, name)
VALUES (@pd_id, 'main');

INSERT INTO vulnerability_metadata_patch_tag (patch_detail_id, name)
VALUES (@pd_id, 'v3.12.14');

-- 9) COSV: contributors/credits/exploit_status
INSERT INTO vulnerability_metadata_contributor (vulnerability_metadata_id, org, name, email, contributions)
VALUES (@vm_id, 'CSPioneer', 'Jeppe', 'jeppe@example.com', 'Report and PoC');

INSERT INTO vulnerability_metadata_credit (vulnerability_metadata_id, name, type)
VALUES (@vm_id, 'Security Team', 'reporter');
SET @credit_id := LAST_INSERT_ID();

INSERT INTO vulnerability_metadata_credit_contact (credit_id, contact)
VALUES (@credit_id, 'mailto:sec@example.com');

INSERT INTO vulnerability_metadata_exploit_status (vulnerability_metadata_id, status)
VALUES (@vm_id, 'exploited_in_the_wild');

COMMIT;

