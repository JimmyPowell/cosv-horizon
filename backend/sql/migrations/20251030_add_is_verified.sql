-- Add is_verified flag to organization table to separate verification from status
ALTER TABLE `organization`
  ADD COLUMN `is_verified` TINYINT(1) NOT NULL DEFAULT 0 AFTER `free_text`;

-- Optional: backfill existing ACTIVE organizations as verified
-- UPDATE organization SET is_verified = 1 WHERE status = 'ACTIVE';

