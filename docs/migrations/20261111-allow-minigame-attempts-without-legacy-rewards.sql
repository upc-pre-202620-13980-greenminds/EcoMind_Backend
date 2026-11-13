-- Legacy attempt rows retain their reward values. Current grants live in Gamification.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'minigame_attempts' AND column_name = 'given_ecopoints'
    ) THEN
        ALTER TABLE minigame_attempts ALTER COLUMN given_ecopoints DROP NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'minigame_attempts' AND column_name = 'given_gems'
    ) THEN
        ALTER TABLE minigame_attempts ALTER COLUMN given_gems DROP NOT NULL;
    END IF;
END
$$;
