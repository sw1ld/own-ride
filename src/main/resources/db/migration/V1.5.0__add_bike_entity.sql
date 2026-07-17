CREATE TABLE bike (
    id UUID PRIMARY KEY,
    producer VARCHAR(255),
    name VARCHAR(255)
);

ALTER TABLE activity_data ADD COLUMN bike_id UUID REFERENCES bike(id);
