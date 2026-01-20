CREATE TABLE IF NOT EXISTS debt_counterparties (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    color VARCHAR(16),
    currency_id BIGINT NOT NULL REFERENCES currencies (id),
    owed_to_me TEXT NOT NULL,
    i_owe TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debt_counterparties_user ON debt_counterparties (user_id);
CREATE INDEX IF NOT EXISTS idx_debt_counterparties_user_status ON debt_counterparties (user_id, status);
