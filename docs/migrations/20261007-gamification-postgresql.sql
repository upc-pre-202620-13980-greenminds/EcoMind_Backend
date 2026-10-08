-- Additive PostgreSQL upgrade of an existing Gamification database.
-- Run with psql -v ON_ERROR_STOP=1. Repeat after startup creates any new tables.
-- Historical audit timestamps/factors are backfilled with their original creation time / 1;
-- a historical multiplier identity cannot be reconstructed and stays NULL.
BEGIN;
DO $$
BEGIN
IF to_regclass('user_progresses') IS NOT NULL THEN
  ALTER TABLE user_progresses ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
END IF;
IF to_regclass('family_scores') IS NOT NULL THEN
  ALTER TABLE family_scores ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
  ALTER TABLE achievement_share_requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
END IF;
IF to_regclass('streak_protection_requests') IS NOT NULL THEN
  ALTER TABLE streak_protection_requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
END IF;
IF to_regclass('user_progresses') IS NOT NULL THEN
  ALTER TABLE user_progresses ADD COLUMN IF NOT EXISTS last_protected_date DATE;
END IF;
IF to_regclass('achievements') IS NOT NULL THEN
  ALTER TABLE achievements ADD COLUMN IF NOT EXISTS cosmetic_id VARCHAR(36);
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  ALTER TABLE reward_transactions ADD COLUMN IF NOT EXISTS user_progress_id BIGINT;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  ALTER TABLE reward_transactions ADD COLUMN IF NOT EXISTS family_score_id BIGINT;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  UPDATE reward_transactions SET user_progress_id=beneficiary_id WHERE beneficiary_type='USER' AND user_progress_id IS NULL;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  UPDATE reward_transactions SET family_score_id=beneficiary_id WHERE beneficiary_type='FAMILY' AND family_score_id IS NULL;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  ALTER TABLE achievement_awards ADD COLUMN IF NOT EXISTS user_progress_id BIGINT;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  ALTER TABLE achievement_awards ADD COLUMN IF NOT EXISTS family_score_id BIGINT;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  UPDATE achievement_awards SET user_progress_id=beneficiary_id WHERE scope='INDIVIDUAL' AND user_progress_id IS NULL;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  UPDATE achievement_awards SET family_score_id=beneficiary_id WHERE scope='FAMILY' AND family_score_id IS NULL;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  ALTER TABLE reward_transactions ADD COLUMN IF NOT EXISTS multiplier_id VARCHAR(36);
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  ALTER TABLE reward_transactions ADD COLUMN IF NOT EXISTS applied_factor NUMERIC(18,8) NOT NULL DEFAULT 1;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
  ALTER TABLE reward_transactions ADD COLUMN IF NOT EXISTS repetition_factor NUMERIC(18,8) NOT NULL DEFAULT 1;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  ALTER TABLE achievement_awards ALTER COLUMN beneficiary_id DROP NOT NULL;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
  ALTER TABLE achievement_awards ADD COLUMN IF NOT EXISTS community_id VARCHAR(36);
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
  ALTER TABLE achievement_share_requests ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMPTZ;
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
  UPDATE achievement_share_requests SET confirmed_at=created_at WHERE status='PUBLISHED' AND confirmed_at IS NULL;
END IF;
IF to_regclass('streak_protection_requests') IS NOT NULL THEN
  ALTER TABLE streak_protection_requests ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMPTZ;
END IF;
IF to_regclass('streak_protection_requests') IS NOT NULL THEN
  UPDATE streak_protection_requests SET resolved_at=created_at WHERE status IN ('PROTECTED','UNAVAILABLE') AND resolved_at IS NULL;
END IF;
IF to_regclass('gem_movements') IS NOT NULL THEN
  ALTER TABLE gem_movements ALTER COLUMN type TYPE VARCHAR(32);
END IF;
IF to_regclass('user_progresses') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_userprogress' AND conrelid='user_progresses'::regclass) THEN
  ALTER TABLE user_progresses ADD CONSTRAINT ck_userprogress CHECK (total_ecopoints >= 0 AND total_experience >= 0 AND current_streak >= 0 AND longest_streak >= current_streak);
END IF;
END IF;
IF to_regclass('family_scores') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_familyscore' AND conrelid='family_scores'::regclass) THEN
  ALTER TABLE family_scores ADD CONSTRAINT ck_familyscore CHECK (total_ecopoints >= 0);
END IF;
END IF;
IF to_regclass('achievements') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_achievement' AND conrelid='achievements'::regclass) THEN
  ALTER TABLE achievements ADD CONSTRAINT ck_achievement CHECK (target > 0 AND scope IN ('INDIVIDUAL','FAMILY','COMMUNITY') AND (cosmetic_id IS NULL OR scope='INDIVIDUAL'));
END IF;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_rewardtransaction' AND conrelid='reward_transactions'::regclass) THEN
  ALTER TABLE reward_transactions ADD CONSTRAINT ck_rewardtransaction CHECK (base_ecopoints >= 0 AND base_experience >= 0 AND base_gems >= 0 AND ecopoints >= 0 AND experience >= 0 AND gems >= 0 AND applied_factor >= 0 AND repetition_factor BETWEEN 0 AND 1 AND source_type IN ('QUEST','MINIGAME','COLLABORATIVE_QUEST','FAMILY_PLAN','COMMUNITY_GOAL','COMMUNITY_EVENT') AND ((beneficiary_type='USER' AND user_progress_id IS NOT NULL AND user_progress_id=beneficiary_id AND family_score_id IS NULL) OR (beneficiary_type='FAMILY' AND family_score_id IS NOT NULL AND family_score_id=beneficiary_id AND user_progress_id IS NULL AND base_experience=0 AND base_gems=0 AND experience=0 AND gems=0)));
END IF;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_award_beneficiary' AND conrelid='achievement_awards'::regclass) THEN
  ALTER TABLE achievement_awards ADD CONSTRAINT ck_award_beneficiary CHECK ((scope='COMMUNITY' AND community_id IS NOT NULL AND beneficiary_id IS NULL AND user_progress_id IS NULL AND family_score_id IS NULL) OR (scope='INDIVIDUAL' AND beneficiary_id IS NOT NULL AND beneficiary_id>0 AND user_progress_id IS NOT NULL AND user_progress_id=beneficiary_id AND family_score_id IS NULL AND community_id IS NULL) OR (scope='FAMILY' AND beneficiary_id IS NOT NULL AND beneficiary_id>0 AND family_score_id IS NOT NULL AND family_score_id=beneficiary_id AND user_progress_id IS NULL AND community_id IS NULL));
END IF;
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_achievementsharerequest' AND conrelid='achievement_share_requests'::regclass) THEN
  ALTER TABLE achievement_share_requests ADD CONSTRAINT ck_achievementsharerequest CHECK (requested_by > 0 AND ((status='PENDING' AND publication_id IS NULL AND confirmed_at IS NULL) OR (status='PUBLISHED' AND publication_id IS NOT NULL AND confirmed_at IS NOT NULL AND confirmed_at >= created_at)));
END IF;
END IF;
IF to_regclass('streak_protection_requests') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ck_streakprotectionrequest' AND conrelid='streak_protection_requests'::regclass) THEN
  ALTER TABLE streak_protection_requests ADD CONSTRAINT ck_streakprotectionrequest CHECK ((status='PENDING' AND resolved_at IS NULL) OR (status IN ('PROTECTED','UNAVAILABLE') AND resolved_at IS NOT NULL AND resolved_at >= created_at));
END IF;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_rewardtransaction_progress' AND conrelid='reward_transactions'::regclass) THEN
  ALTER TABLE reward_transactions ADD CONSTRAINT fk_rewardtransaction_progress FOREIGN KEY(user_progress_id) REFERENCES user_progresses(user_id);
END IF;
END IF;
IF to_regclass('reward_transactions') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_rewardtransaction_family' AND conrelid='reward_transactions'::regclass) THEN
  ALTER TABLE reward_transactions ADD CONSTRAINT fk_rewardtransaction_family FOREIGN KEY(family_score_id) REFERENCES family_scores(family_id);
END IF;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_achievementaward_progress' AND conrelid='achievement_awards'::regclass) THEN
  ALTER TABLE achievement_awards ADD CONSTRAINT fk_achievementaward_progress FOREIGN KEY(user_progress_id) REFERENCES user_progresses(user_id);
END IF;
END IF;
IF to_regclass('achievement_awards') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_achievementaward_family' AND conrelid='achievement_awards'::regclass) THEN
  ALTER TABLE achievement_awards ADD CONSTRAINT fk_achievementaward_family FOREIGN KEY(family_score_id) REFERENCES family_scores(family_id);
END IF;
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_share_award' AND conrelid='achievement_share_requests'::regclass) THEN
  ALTER TABLE achievement_share_requests ADD CONSTRAINT fk_share_award FOREIGN KEY(award_id) REFERENCES achievement_awards(id);
END IF;
END IF;
IF to_regclass('achievement_share_requests') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='uq_share_publication' AND conrelid='achievement_share_requests'::regclass) THEN
  ALTER TABLE achievement_share_requests ADD CONSTRAINT uq_share_publication UNIQUE(publication_id);
END IF;
END IF;
IF to_regclass('streak_protection_requests') IS NOT NULL THEN
IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_protection_progress' AND conrelid='streak_protection_requests'::regclass) THEN
  ALTER TABLE streak_protection_requests ADD CONSTRAINT fk_protection_progress FOREIGN KEY(user_id) REFERENCES user_progresses(user_id);
END IF;
END IF;
END $$;
COMMIT;
