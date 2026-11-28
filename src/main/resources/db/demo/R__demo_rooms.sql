-- Dev-only demo data, loaded through spring.flyway.locations in application-dev.yaml.
-- Repeatable migration: re-applied only when this file changes. Fixed ids so docs can reference them.
INSERT INTO room (id, name, capacity, created_at, version) VALUES
('00000000-0000-0000-0000-000000000001', 'Atlas',   4, now(), 0),
('00000000-0000-0000-0000-000000000002', 'Boreal',  8, now(), 0),
('00000000-0000-0000-0000-000000000003', 'Cosmos', 12, now(), 0),
('00000000-0000-0000-0000-000000000004', 'Delta',  20, now(), 0)
ON CONFLICT (id) DO NOTHING;