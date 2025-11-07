-- create table if not exists transactions (
--                                             id uuid primary key,
--                                             idem_key varchar(120) not null unique,
--     amount numeric(18,2) not null,
--     payload jsonb not null,
--     status varchar(32) not null,
--     attempts int not null default 0,
--     created_at timestamptz not null default now(),
--     updated_at timestamptz not null default now()
--     );
--
-- create index if not exists idx_transactions_status on transactions(status);

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