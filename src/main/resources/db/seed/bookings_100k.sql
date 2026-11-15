-- NOT a Flyway migration: run manually against the dev DB only.
BEGIN;

SELECT setseed(0.42);

INSERT INTO room (id, name, capacity, created_at, version)
SELECT gen_random_uuid(),
       'Bench room ' || lpad(i::text, 2, '0'),
       2 + (i % 10) * 2,
       now(),
       0
FROM generate_series(1, 50) AS i;

INSERT INTO booking (id, room_id, starts_at, ends_at, user_name)
SELECT gen_random_uuid(),
       r.id,
       timestamptz '2030-01-01 00:00Z' + n * interval '1 hour',
       timestamptz '2030-01-01 00:00Z' + (n + 1) * interval '1 hour',
       'bench'
FROM room r
    CROSS JOIN generate_series(0, 2499) AS n
WHERE r.name LIKE 'Bench room %'
  AND random() < 0.8;

COMMIT;

ANALYZE room;
ANALYZE booking;