INSERT INTO drivers (id, first_name, last_name, phone, status, created_at)
VALUES
  ('d1e2f3a4-0001-0001-0001-000000000001', 'Max',   'Müller',  '+49 30 11111111', 'AVAILABLE', NOW()),
  ('d1e2f3a4-0002-0002-0002-000000000002', 'Lisa',  'Schmidt', '+49 30 22222222', 'AVAILABLE', NOW()),
  ('d1e2f3a4-0003-0003-0003-000000000003', 'Tom',   'Wagner',  '+49 30 33333333', 'OFFLINE',   NOW())
ON CONFLICT DO NOTHING;

-- Unassigned delivery for Anna Müller's order from Bella Italia (see customer-service/restaurant-service/
-- order-service data.sql — order_id matches the READY_FOR_DELIVERY order seeded in order-service). It's a
-- self-contained fixture for exercising DeliveryController manually / via Postman without needing the full
-- order→restaurant→delivery Kafka flow. PENDING + driver_id NULL matches what OrderReadyConsumer.receiveOrder()
-- would create when no AVAILABLE driver was free at the time.
INSERT INTO delivery_orders (id, order_id, customer_id, restaurant_id, driver_id,
                              pickup_street, pickup_house_number, pickup_city, pickup_zip, pickup_country,
                              street, house_number, city, zip, country,
                              status, created_at, updated_at)
VALUES
  ('e2f3a4b5-0001-0001-0001-000000000001', 'f1a2b3c4-0001-0001-0001-000000000001',
   'a1b2c3d4-0001-0001-0001-000000000001', 'b1c2d3e4-0001-0001-0001-000000000001', NULL,
   'Friedrichstraße', '42', 'Berlin', '10117', 'Germany',
   'Hauptstraße', '12', 'Berlin', '10115', 'Germany',
   'PENDING', NOW(), NOW())
ON CONFLICT DO NOTHING;
