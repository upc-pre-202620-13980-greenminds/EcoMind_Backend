BEGIN;

-- Second value of the generated texts and the key of the source that produced the notification
ALTER TABLE public.notifications
    ADD COLUMN IF NOT EXISTS detail VARCHAR(150),
    ADD COLUMN IF NOT EXISTS source_key VARCHAR(120);

-- The same source never notifies a recipient twice; rows without a key do not take part
ALTER TABLE public.notifications
    ADD CONSTRAINT uk_notifications_recipient_source UNIQUE (recipient_id, source_key);

COMMIT;
