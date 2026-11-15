BEGIN;
DELETE FROM booking WHERE room_id IN (SELECT id FROM room WHERE name LIKE 'Bench room %');
DELETE FROM room WHERE name LIKE 'Bench room %';
COMMIT;
ANALYZE room;
ANALYZE booking;