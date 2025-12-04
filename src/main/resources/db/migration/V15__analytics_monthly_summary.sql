CREATE TABLE IF NOT EXISTS analytics_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    total_expense TEXT NOT NULL,
    total_income TEXT NOT NULL,
    cash_flow_amount TEXT NOT NULL,
    cash_flow_percent NUMERIC(8, 2) NOT NULL,
    expense_breakdown TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_analytics_month UNIQUE (user_id, period_start)
);

CREATE INDEX IF NOT EXISTS idx_analytics_month_user ON analytics_monthly_summary (user_id);
CREATE INDEX IF NOT EXISTS idx_analytics_month_period ON analytics_monthly_summary (period_start);
