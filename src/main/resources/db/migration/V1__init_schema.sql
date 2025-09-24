CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT NOT NULL UNIQUE,
    telegram_username VARCHAR(255) NOT NULL,
    telegram_language_code VARCHAR(16),
    role VARCHAR(50) NOT NULL,
    subscription_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_telegram_user_id ON users (telegram_user_id);

CREATE TABLE IF NOT EXISTS currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    decimal_precision SMALLINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO currencies (code, name, decimal_precision, active)
VALUES
    ('USD', 'US Dollar', 2, TRUE),
    ('KZT', 'Kazakhstani Tenge', 2, TRUE),
    ('EUR', 'Euro', 2, TRUE),
    ('RUB', 'Russian Ruble', 2, TRUE),
    ('CNY', 'Chinese Yuan', 2, TRUE),
    ('AED', 'UAE Dirham', 2, TRUE)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    currency_id BIGINT NOT NULL REFERENCES currencies (id),
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets (user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_currency_id ON wallets (currency_id);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    base_currency_id BIGINT NOT NULL REFERENCES currencies (id),
    target_currency_id BIGINT NOT NULL REFERENCES currencies (id),
    rate NUMERIC(18, 6) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(100) NOT NULL,
    CONSTRAINT uq_exchange_rate_unique UNIQUE (base_currency_id, target_currency_id, fetched_at)
);

CREATE INDEX IF NOT EXISTS idx_exchange_rates_base_target ON exchange_rates (base_currency_id, target_currency_id, fetched_at DESC);

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
