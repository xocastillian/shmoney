CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    from_wallet_id BIGINT NOT NULL REFERENCES wallets (id),
    to_wallet_id BIGINT NOT NULL REFERENCES wallets (id),
    source_currency_id BIGINT NOT NULL REFERENCES currencies (id),
    target_currency_id BIGINT NOT NULL REFERENCES currencies (id),
    source_amount NUMERIC(18, 2) NOT NULL,
    target_amount NUMERIC(18, 2) NOT NULL,
    exchange_rate NUMERIC(18, 6) NOT NULL,
    description VARCHAR(255),
    executed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_wallet_transactions_distinct_wallets CHECK (from_wallet_id <> to_wallet_id)
);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_from ON wallet_transactions (from_wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_to ON wallet_transactions (to_wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_executed_at ON wallet_transactions (executed_at DESC);
