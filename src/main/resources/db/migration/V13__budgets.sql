CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    budget_type VARCHAR(20) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    amount_limit TEXT NOT NULL,
    spent_amount TEXT NOT NULL,
    percent_spent NUMERIC(8, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS budget_categories (
    budget_id BIGINT NOT NULL REFERENCES budgets (id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    PRIMARY KEY (budget_id, category_id)
);

CREATE INDEX IF NOT EXISTS idx_budgets_owner_status ON budgets (owner_id, status);
CREATE INDEX IF NOT EXISTS idx_budgets_owner_period ON budgets (owner_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_budget_categories_category ON budget_categories (category_id);
