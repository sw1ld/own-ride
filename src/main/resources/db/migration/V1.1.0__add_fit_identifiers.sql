ALTER TABLE activity_data ADD COLUMN time_created TIMESTAMP;
CREATE UNIQUE INDEX idx_activity_data_time_created ON activity_data (time_created);
