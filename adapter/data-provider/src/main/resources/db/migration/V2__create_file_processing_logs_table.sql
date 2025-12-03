CREATE TABLE IF NOT EXISTS file_processing_logs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_lines INTEGER DEFAULT 0,
    processed_lines INTEGER DEFAULT 0,
    error_lines INTEGER DEFAULT 0,
    errors JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_logs_status ON file_processing_logs(status);
CREATE INDEX IF NOT EXISTS idx_logs_uploaded_at ON file_processing_logs(uploaded_at DESC);
CREATE INDEX IF NOT EXISTS idx_logs_errors ON file_processing_logs USING GIN (errors);
