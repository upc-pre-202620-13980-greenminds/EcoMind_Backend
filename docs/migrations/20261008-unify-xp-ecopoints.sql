-- Team clarification: XP is ecopoints. Apply with the unified backend deployment.
-- Keep historical grants and canonical balances; do not add the former XP values.
-- Obsolete XP columns/table are retained for rollback/schema compatibility, not read by the domain.
BEGIN;
UPDATE achievements SET metric='ECOPOINTS' WHERE metric='EXPERIENCE';
COMMIT;
