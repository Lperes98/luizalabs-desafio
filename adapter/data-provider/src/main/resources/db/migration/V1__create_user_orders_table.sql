CREATE TABLE IF NOT EXISTS user_orders (
    user_id BIGINT PRIMARY KEY,
    name VARCHAR(45) NOT NULL,
    orders JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_gin ON user_orders USING GIN (orders);
CREATE INDEX IF NOT EXISTS idx_orders_date ON user_orders USING GIN ((orders));

