-- Migration: add uuid + code to tag, and backfill
-- Safe for MySQL / MariaDB. Review in staging before running on prod.

-- 1) Add columns (nullable first to allow backfill), then set NOT NULL after backfill.
ALTER TABLE tag ADD COLUMN uuid VARCHAR(36) NULL;
ALTER TABLE tag ADD COLUMN code VARCHAR(64) NULL;

-- 2) Backfill values for existing rows
-- Generate UUIDs
UPDATE tag SET uuid = (SELECT UUID()) WHERE uuid IS NULL;

-- Backfill code by slugifying name (basic, lowercase, replace spaces with '-')
-- Note: This is simplistic; adapt if you need richer transliteration.
UPDATE tag SET code = LOWER(REPLACE(name, ' ', '-')) WHERE code IS NULL;

-- 3) Resolve code collisions by appending numeric suffixes (naive example)
-- This step is best handled with a custom script if collisions exist.
-- You can inspect duplicates via:
--   SELECT code, COUNT(*) c FROM tag GROUP BY code HAVING c > 1;

-- 4) Enforce uniqueness and NOT NULL constraints
ALTER TABLE tag ADD CONSTRAINT uq_tag_uuid UNIQUE (uuid);
ALTER TABLE tag ADD CONSTRAINT uq_tag_code UNIQUE (code);
ALTER TABLE tag MODIFY uuid VARCHAR(36) NOT NULL;
ALTER TABLE tag MODIFY code VARCHAR(64) NOT NULL;

-- 5) (Optional) Keep or relax name uniqueness per product decision
-- Currently schema keeps name UNIQUE; adjust if needed.

