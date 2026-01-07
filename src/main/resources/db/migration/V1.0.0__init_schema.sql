CREATE TABLE activity_raw (
  id UUID PRIMARY KEY,
  name varchar(70) NOT NULL,
  fit_file BYTEA NOT NULL,
  uploaded_at TIMESTAMP NOT NULL,
  last_modified TIMESTAMP NOT NULL
);

CREATE TABLE activity_data (
    id UUID PRIMARY KEY,
    activity_raw_id UUID NOT NULL CONSTRAINT activity_raw_id_fkey references activity_raw (id),
    name varchar(70) NOT NULL,
    date DATE NOT NULL,
    distance DOUBLE PRECISION,
    duration NUMERIC(21),
    avg_speed DOUBLE PRECISION,
    max_speed DOUBLE PRECISION,
    total_ascent INTEGER,
    temperature INTEGER,
    positions JSONB
);



