ALTER TABLE app_settings
    ADD COLUMN IF NOT EXISTS main_currency VARCHAR(10);

UPDATE app_settings
SET main_currency = 'KZT'
WHERE main_currency IS NULL;

ALTER TABLE app_settings
    ALTER COLUMN main_currency SET NOT NULL,
    ALTER COLUMN main_currency SET DEFAULT 'KZT';
