-- Allow personal uploads: make raw_cosv_file.organization_id nullable
ALTER TABLE raw_cosv_file
  MODIFY COLUMN organization_id BIGINT NULL;

