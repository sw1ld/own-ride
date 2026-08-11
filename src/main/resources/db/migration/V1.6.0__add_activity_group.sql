ALTER TABLE activity_data ADD COLUMN thumbnail TEXT;

CREATE TABLE activity_group (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    rate INTEGER,
    thumbnail TEXT
);

ALTER TABLE activity_data ADD COLUMN group_id UUID REFERENCES activity_group(id);
