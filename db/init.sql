CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    idem_key VARCHAR(120) NOT NULL UNIQUE,
    amount NUMERIC(18,2) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);