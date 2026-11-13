BEGIN;

ALTER TABLE public.community_communities
    ALTER COLUMN created_by DROP NOT NULL;

COMMIT;