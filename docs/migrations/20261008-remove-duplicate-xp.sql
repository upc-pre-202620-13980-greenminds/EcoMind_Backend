-- XP and ecopoints are one score. Run before starting the backend with this change.
-- Ecopoints, gems, reward history and achievement ownership remain unchanged.
BEGIN;
UPDATE achievements SET metric='ECOPOINTS' WHERE metric='EXPERIENCE';

ALTER TABLE user_progresses DROP CONSTRAINT IF EXISTS ck_userprogress;
ALTER TABLE reward_transactions DROP CONSTRAINT IF EXISTS ck_rewardtransaction;

ALTER TABLE user_progresses DROP COLUMN IF EXISTS total_experience;
ALTER TABLE reward_transactions DROP COLUMN IF EXISTS base_experience;
ALTER TABLE reward_transactions DROP COLUMN IF EXISTS experience;
DROP TABLE IF EXISTS gamification_quest_experiences;

ALTER TABLE user_progresses ADD CONSTRAINT ck_userprogress CHECK (
  total_ecopoints >= 0 AND current_streak >= 0 AND longest_streak >= current_streak
);
ALTER TABLE reward_transactions ADD CONSTRAINT ck_rewardtransaction CHECK (
  base_ecopoints >= 0 AND base_gems >= 0 AND ecopoints >= 0 AND gems >= 0
  AND applied_factor >= 0 AND repetition_factor BETWEEN 0 AND 1
  AND source_type IN ('QUEST','MINIGAME','COLLABORATIVE_QUEST','FAMILY_PLAN','COMMUNITY_GOAL','COMMUNITY_EVENT')
  AND (
    (beneficiary_type='USER' AND user_progress_id IS NOT NULL
      AND user_progress_id=beneficiary_id AND family_score_id IS NULL)
    OR (beneficiary_type='FAMILY' AND family_score_id IS NOT NULL
      AND family_score_id=beneficiary_id AND user_progress_id IS NULL AND base_gems=0 AND gems=0)
  )
);
COMMIT;
