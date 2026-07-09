INSERT INTO customers (id, first_name, last_name, email, phone, street, house_number, city, zip, country,
                       created_at)
VALUES ('a1b2c3d4-0001-0001-0001-000000000001', 'Anna', 'Müller', 'anna.mueller@example.com',
        '+49 30 12345678', 'Hauptstraße', '12', 'Berlin', '10115', 'Germany', NOW()),
       ('a1b2c3d4-0002-0002-0002-000000000002', 'Ben', 'Schmidt', 'ben.schmidt@example.com',
        '+49 89 23456789', 'Leopoldstraße', '5A', 'Munich', '80802', 'Germany', NOW()),
       ('a1b2c3d4-0003-0003-0003-000000000003', 'Clara', 'Weber', 'clara.weber@example.com',
        '+49 40 34567890', 'Mönckebergstraße', '3', 'Hamburg', '20095', 'Germany', NOW()),
       ('a1b2c3d4-0003-0003-0003-000000000004', 'Veit', 'Ullmann', 'veitullmann@gmx.de',
        '+491638475447', 'Bornholmer Str.', '74', 'Berlin', '10439', 'Germany', NOW())
ON CONFLICT DO NOTHING;
