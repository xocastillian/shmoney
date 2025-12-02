DROP VIEW IF EXISTS user_transaction_feed;

ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

UPDATE categories SET status = 'ACTIVE' WHERE status IS NULL;

ALTER TABLE categories
    ALTER COLUMN status SET DEFAULT 'ACTIVE',
    ALTER COLUMN status SET NOT NULL;

DROP INDEX IF EXISTS idx_category_transactions_subcategory;

ALTER TABLE category_transactions
    DROP CONSTRAINT IF EXISTS category_transactions_subcategory_id_fkey,
    DROP COLUMN IF EXISTS subcategory_id;

DROP TABLE IF EXISTS subcategories;

CREATE OR REPLACE VIEW user_transaction_feed AS
SELECT
    'CATEGORY'::text AS entry_source,
    ct.id AS entry_id,
    ct.user_id,
    ct.wallet_id,
    NULL::BIGINT AS counterparty_wallet_id,
    ct.category_id,
    ct.type AS category_transaction_type,
    ct.amount,
    curr.code AS currency_code,
    ct.description,
    ct.occurred_at,
    ct.created_at,
    ARRAY[ct.wallet_id]::BIGINT[] AS wallet_ids
FROM category_transactions ct
JOIN currencies curr ON curr.id = ct.currency_id
UNION ALL
SELECT
    'TRANSFER'::text AS entry_source,
    wt.id AS entry_id,
    u.id AS user_id,
    wt.from_wallet_id AS wallet_id,
    wt.to_wallet_id AS counterparty_wallet_id,
    NULL::BIGINT AS category_id,
    'TRANSFER'::text AS category_transaction_type,
    wt.source_amount AS amount,
    src.code AS currency_code,
    wt.description,
    wt.executed_at AS occurred_at,
    wt.created_at,
    ARRAY[wt.from_wallet_id, wt.to_wallet_id]::BIGINT[] AS wallet_ids
FROM wallet_transactions wt
JOIN wallets wf ON wf.id = wt.from_wallet_id
JOIN users u ON u.id = wf.user_id
JOIN currencies src ON src.id = wt.source_currency_id;
