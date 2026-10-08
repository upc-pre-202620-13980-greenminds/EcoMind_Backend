-- Team clarification: XP is ecopoints. Apply with the unified backend deployment.
-- Keep historical grants and canonical balances; do not add the former XP values.
-- Follow with 20261008-remove-duplicate-xp.sql to remove the duplicated XP storage.
BEGIN;
UPDATE achievements SET metric='ECOPOINTS' WHERE metric='EXPERIENCE';
COMMIT;
