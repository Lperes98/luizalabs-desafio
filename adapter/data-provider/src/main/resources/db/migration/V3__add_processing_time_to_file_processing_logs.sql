ALTER TABLE file_processing_logs
ADD COLUMN IF NOT EXISTS processing_time_ms BIGINT;
