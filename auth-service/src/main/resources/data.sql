INSERT INTO user_credentials (id, user_id, email, hashed_password, first_name, last_name, phone, role, created_at)
VALUES ('e1f2a3b4-0001-0001-0001-000000000001', 'a1b2c3d4-0001-0001-0001-000000000001', 'anna.mueller@example.com',
        '$2a$10$HvpoKLcMPkks339Cge.kheG/fwDd3gNGTX8.q96wZaZpyo9efjEEO', 'Anna', 'Müller', '+491700000001',
        'CUSTOMER', NOW()),
       ('e1f2a3b4-0002-0002-0002-000000000002', 'a1b2c3d4-0002-0002-0002-000000000002', 'ben.schmidt@example.com',
        '$2a$10$aNmNNil9j5wlEen5dPws1OgxVfyyP3Njum/jtjyWbnUAPQykcAFs.', 'Ben', 'Schmidt', '+491700000002',
        'CUSTOMER', NOW()),
       ('e1f2a3b4-0003-0003-0003-000000000003', 'a1b2c3d4-0003-0003-0003-000000000003', 'clara.weber@example.com',
        '$2a$10$Inh3trBsQwFDP1VW9GeRw.unwMZMDskjV9XMdkMlvJ/vVe3Skq58.', 'Clara', 'Weber', '+491700000003',
        'CUSTOMER', NOW()),
       ('e1f2a3b4-0004-0004-0004-000000000004', 'a1b2c3d4-0003-0003-0003-000000000004', 'veitullmann@gmx.de',
        '$2a$10$iuBSezkdGP9mowTb39PPOOpGhGpB53HpI6LGXys4JsTnaGbjNrdZu', 'Veit', 'Ullmann', '+491700000004',
        'SUPER_ADMIN', NOW())
ON CONFLICT DO NOTHING;
