CREATE TABLE IF NOT EXISTS category_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    subcategory_id BIGINT REFERENCES subcategories (id) ON DELETE SET NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('EXPENSE', 'INCOME')),
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    currency_id BIGINT NOT NULL REFERENCES currencies (id),
    description VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_category_transactions_user ON category_transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_category_transactions_wallet ON category_transactions (wallet_id);
CREATE INDEX IF NOT EXISTS idx_category_transactions_category ON category_transactions (category_id);
CREATE INDEX IF NOT EXISTS idx_category_transactions_subcategory ON category_transactions (subcategory_id);
CREATE INDEX IF NOT EXISTS idx_category_transactions_occurred_at ON category_transactions (occurred_at);

CREATE OR REPLACE VIEW user_transaction_feed AS
SELECT
    'CATEGORY'::text AS entry_source,
    ct.id AS entry_id,
    ct.user_id,
    ct.wallet_id,
    NULL::BIGINT AS counterparty_wallet_id,
    ct.category_id,
    ct.subcategory_id,
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
    NULL::BIGINT AS subcategory_id,
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
