ALTER TABLE wallets
    ALTER COLUMN color SET DEFAULT '#202020';

UPDATE wallets
SET color = '#202020'
WHERE color IS NULL OR color = '#6B7280';
