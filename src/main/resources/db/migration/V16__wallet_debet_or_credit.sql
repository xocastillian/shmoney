ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS debet_or_credit VARCHAR(10) NOT NULL DEFAULT 'DEBET';

UPDATE wallets
SET debet_or_credit = 'DEBET'
WHERE debet_or_credit IS NULL;
