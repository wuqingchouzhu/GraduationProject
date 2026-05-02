ALTER TABLE stock_in_record ADD COLUMN IF NOT EXISTS message_id VARCHAR(36) DEFAULT NULL;
ALTER TABLE stock_out_record ADD COLUMN IF NOT EXISTS message_id VARCHAR(36) DEFAULT NULL;
ALTER TABLE failed_messages ADD COLUMN IF NOT EXISTS message_id VARCHAR(36) DEFAULT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_stock_in_message_id ON stock_in_record(message_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_stock_out_message_id ON stock_out_record(message_id);
CREATE INDEX IF NOT EXISTS idx_failed_messages_message_id ON failed_messages(message_id);
