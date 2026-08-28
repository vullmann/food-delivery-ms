-- Restaurants
INSERT INTO restaurants (id, name, description, street, house_number, city, zip, country, phone, email, cuisine_type, is_open, created_at)
VALUES
    ('b1c2d3e4-0001-0001-0001-000000000001', 'Bella Italia',    'Authentic Italian cuisine in the heart of Berlin',    'Friedrichstraße', '42',  'Berlin',  '10117', 'Germany', '+49 30 11112222', 'info@bella-italia.de',   'ITALIAN', true,  NOW()),
    ('b1c2d3e4-0002-0002-0002-000000000002', 'Burger Palace',   'Handcrafted burgers with locally sourced ingredients', 'Schloßstraße',    '10',  'Munich',  '80333', 'Germany', '+49 89 33334444', 'hello@burgerpalace.de',  'BURGER',  true,  NOW()),
    ('b1c2d3e4-0003-0003-0003-000000000003', 'Tokyo Garden',    'Traditional Japanese and Sushi specialties',          'Reeperbahn',      '88',  'Hamburg', '20359', 'Germany', '+49 40 55556666', 'contact@tokyo-garden.de','SUSHI',   false, NOW())
ON CONFLICT DO NOTHING;

-- Menu items – Bella Italia
INSERT INTO menu_items (id, restaurant_id, name, description, price, category, available)
VALUES
    ('c1d2e3f4-0101-0101-0101-000000000001', 'b1c2d3e4-0001-0001-0001-000000000001', 'Bruschetta',        'Toasted bread with tomatoes and basil',          4.50,  'STARTER', true),
    ('c1d2e3f4-0102-0102-0102-000000000002', 'b1c2d3e4-0001-0001-0001-000000000001', 'Spaghetti Carbonara','Classic carbonara with guanciale and pecorino', 13.90, 'MAIN',    true),
    ('c1d2e3f4-0103-0103-0103-000000000003', 'b1c2d3e4-0001-0001-0001-000000000001', 'Tiramisu',          'Homemade tiramisu with mascarpone',              6.50,  'DESSERT', true),
    ('c1d2e3f4-0104-0104-0104-000000000004', 'b1c2d3e4-0001-0001-0001-000000000001', 'Acqua Panna 0.5l',  'Still mineral water',                            3.00,  'DRINK',   true)
ON CONFLICT DO NOTHING;

-- Menu items – Burger Palace
INSERT INTO menu_items (id, restaurant_id, name, description, price, category, available)
VALUES
    ('c1d2e3f4-0201-0201-0201-000000000005', 'b1c2d3e4-0002-0002-0002-000000000002', 'Onion Rings',       'Crispy fried onion rings with dipping sauce',    5.00,  'STARTER', true),
    ('c1d2e3f4-0202-0202-0202-000000000006', 'b1c2d3e4-0002-0002-0002-000000000002', 'Classic Cheeseburger','Double patty, cheddar, pickles, house sauce',  12.50, 'MAIN',    true),
    ('c1d2e3f4-0203-0203-0203-000000000007', 'b1c2d3e4-0002-0002-0002-000000000002', 'BBQ Bacon Burger',  'Smoked bacon, BBQ sauce, caramelised onions',    14.50, 'MAIN',    true),
    ('c1d2e3f4-0204-0204-0204-000000000008', 'b1c2d3e4-0002-0002-0002-000000000002', 'Craft Cola 0.33l',  'Local craft cola',                               3.50,  'DRINK',   true)
ON CONFLICT DO NOTHING;

-- Menu items – Tokyo Garden
INSERT INTO menu_items (id, restaurant_id, name, description, price, category, available)
VALUES
    ('c1d2e3f4-0301-0301-0301-000000000009', 'b1c2d3e4-0003-0003-0003-000000000003', 'Edamame',           'Steamed salted soybeans',                        4.00,  'STARTER', true),
    ('c1d2e3f4-0302-0302-0302-000000000010', 'b1c2d3e4-0003-0003-0003-000000000003', 'Salmon Nigiri (2x)','Fresh Atlantic salmon on seasoned rice',          8.50,  'MAIN',    true),
    ('c1d2e3f4-0303-0303-0303-000000000011', 'b1c2d3e4-0003-0003-0003-000000000003', 'Spicy Tuna Roll',   'Tuna, cucumber, sriracha mayo (8 pcs)',           11.00, 'MAIN',    false),
    ('c1d2e3f4-0304-0304-0304-000000000012', 'b1c2d3e4-0003-0003-0003-000000000003', 'Mochi Ice Cream',   'Green tea mochi with vanilla filling',            5.50,  'DESSERT', true),
    ('c1d2e3f4-0305-0305-0305-000000000013', 'b1c2d3e4-0003-0003-0003-000000000003', 'Japanese Green Tea','Freshly brewed sencha',                           3.00,  'DRINK',   true)
ON CONFLICT DO NOTHING;

-- Restaurant-side mirror of Anna Müller's order at Bella Italia (see customer-service/order-service/
-- delivery-service data.sql — customer_order_id matches the READY_FOR_DELIVERY order in order-service).
-- READY_FOR_DELIVERY matches the order-service status and the unassigned PENDING delivery in delivery-service.
INSERT INTO restaurant_orders (id, customer_order_id, restaurant_id, customer_id,
                                street, house_number, city, zip, country,
                                status, created_at)
VALUES
    ('f2a3b4c5-0001-0001-0001-000000000001', 'f1a2b3c4-0001-0001-0001-000000000001',
     'b1c2d3e4-0001-0001-0001-000000000001', 'a1b2c3d4-0001-0001-0001-000000000001',
     'Hauptstraße', '12', 'Berlin', '10115', 'Germany',
     'READY_FOR_DELIVERY', NOW())
ON CONFLICT DO NOTHING;

INSERT INTO restaurant_order_items (id, restaurant_order_id, menu_item_id, name, quantity, unit_price, total_price)
VALUES
    ('f3a4b5c6-0001-0001-0001-000000000001', 'f2a3b4c5-0001-0001-0001-000000000001',
     'c1d2e3f4-0102-0102-0102-000000000002', 'Spaghetti Carbonara', 1, 13.90, 13.90),
    ('f3a4b5c6-0002-0002-0002-000000000002', 'f2a3b4c5-0001-0001-0001-000000000001',
     'c1d2e3f4-0103-0103-0103-000000000003', 'Tiramisu', 1, 6.50, 6.50)
ON CONFLICT DO NOTHING;
