CREATE TABLE booking (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES room(id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    check ( ends_at > starts_at )
);