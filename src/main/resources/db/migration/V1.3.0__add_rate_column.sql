ALTER TABLE activity_data ADD COLUMN rate INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE activity_data ADD CONSTRAINT check_rate_range CHECK (rate >= 0 AND rate <= 5);
