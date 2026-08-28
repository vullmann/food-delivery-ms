-- Anna Müller's order at Bella Italia (see customer-service/restaurant-service data.sql).
-- READY_FOR_DELIVERY: order is ready, awaiting driver assignment — matches the PENDING,
-- driver-less delivery fixture in delivery-service/src/main/resources/data.sql, which
-- references this order's id.
INSERT INTO customer_orders (id, created_at, updated_at, customer_id, restaurant_id,
                              street, house_number, city, zip, country,
                              total_amount, status)
VALUES
    ('f1a2b3c4-0001-0001-0001-000000000001', NOW(), NOW(),
     'a1b2c3d4-0001-0001-0001-000000000001', 'b1c2d3e4-0001-0001-0001-000000000001',
     'Hauptstraße', '12', 'Berlin', '10115', 'Germany',
     20.40, 'READY_FOR_DELIVERY')
ON CONFLICT DO NOTHING;

INSERT INTO customer_order_items (id, customer_order_id, menu_item_id, name, description, quantity, unit_price, total_price)
VALUES
    ('d1e2f3a4-0101-0101-0101-000000000001', 'f1a2b3c4-0001-0001-0001-000000000001',
     'c1d2e3f4-0102-0102-0102-000000000002', 'Spaghetti Carbonara', 'Classic carbonara with guanciale and pecorino',
     1, 13.90, 13.90),
    ('d1e2f3a4-0102-0102-0102-000000000002', 'f1a2b3c4-0001-0001-0001-000000000001',
     'c1d2e3f4-0103-0103-0103-000000000003', 'Tiramisu', 'Homemade tiramisu with mascarpone',
     1, 6.50, 6.50)
ON CONFLICT DO NOTHING;
