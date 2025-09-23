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

ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS currency_id BIGINT;

UPDATE wallets w
SET currency_id = c.id
FROM currencies c
WHERE w.currency_id IS NULL
  AND UPPER(w.currency) = UPPER(c.code);

UPDATE wallets
SET currency_id = (SELECT id FROM currencies WHERE code = 'USD')
WHERE currency_id IS NULL;

ALTER TABLE wallets
    ALTER COLUMN currency_id SET NOT NULL,
    ADD CONSTRAINT fk_wallets_currency FOREIGN KEY (currency_id) REFERENCES currencies (id);

ALTER TABLE wallets
    DROP COLUMN IF EXISTS currency;

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
