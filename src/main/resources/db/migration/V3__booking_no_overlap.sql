CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE booking ADD CONSTRAINT booking_no_overlap
    EXCLUDE USING gist (room_id WITH =, tstzrange(starts_at, ends_at) WITH &&);