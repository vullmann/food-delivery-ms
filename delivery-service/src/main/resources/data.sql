INSERT INTO drivers (id, first_name, last_name, phone, status, created_at)
VALUES
  ('d1e2f3a4-0001-0001-0001-000000000001', 'Max',   'Müller',  '+49 30 11111111', 'AVAILABLE', NOW()),
  ('d1e2f3a4-0002-0002-0002-000000000002', 'Lisa',  'Schmidt', '+49 30 22222222', 'AVAILABLE', NOW()),
  ('d1e2f3a4-0003-0003-0003-000000000003', 'Tom',   'Wagner',  '+49 30 33333333', 'OFFLINE',   NOW())
ON CONFLICT DO NOTHING;
