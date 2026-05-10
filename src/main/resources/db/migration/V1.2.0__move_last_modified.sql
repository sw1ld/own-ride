ALTER TABLE activity_raw DROP COLUMN last_modified;
ALTER TABLE activity_data ADD COLUMN last_modified TIMESTAMP;
UPDATE activity_data SET last_modified = time_created;
