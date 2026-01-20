CREATE TABLE IF NOT EXISTS debt_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    counterparty_id BIGINT NOT NULL REFERENCES debt_counterparties (id) ON DELETE CASCADE,
    wallet_id BIGINT NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    direction VARCHAR(16) NOT NULL,
    amount TEXT NOT NULL,
    currency_id BIGINT NOT NULL REFERENCES currencies (id),
    description VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debt_transactions_user ON debt_transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_debt_transactions_counterparty ON debt_transactions (counterparty_id);
CREATE INDEX IF NOT EXISTS idx_debt_transactions_occurred_at ON debt_transactions (occurred_at);
