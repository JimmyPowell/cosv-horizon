-- Migration: Enhance search over aliases/summary/identifier
-- Date: 2025-10-30

-- Index to accelerate alias prefix search
CREATE INDEX IF NOT EXISTS idx_vm_alias_value ON vulnerability_metadata_alias(value);

