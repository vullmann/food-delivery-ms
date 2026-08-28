# Food Delivery Service

A microservices-based food delivery platform built with Spring Boot 4 and Spring Cloud.

## Architecture

```
External client (HTTP :8088)
        │
    api-gateway  ← validates JWT, routes by path prefix
        │  lb:// via Eureka
        ├──► auth-service        (8084)  /auth/**          (public)
        ├──► customer-service    (8081)  /customers/**     (JWT)
        ├──► restaurant-service  (8082)  /restaurants/**   (JWT)
        ├──► order-service       (8080)  /orders/**        (JWT)
        ├──► delivery-service    (8083)  /deliveries/**    (JWT)
        │                                /drivers/**       (JWT)
        └──► chat-service        (8085)  /chat/**          (JWT)
                  └──► mcp-service (8090) ─ REST ──► all services

Kafka (internal)
    order-service ──► restaurant-service ──► delivery-service
                                         └──► order-service (status updates)
                                         └──► notification-service (WhatsApp)

discovery-server (8761) ← all services register here
```

## Services

| Service              | Port | Description                                      |
|----------------------|------|--------------------------------------------------|
| api-gateway          | 8088 | Single entry point, JWT validation, routing      |
| auth-service         | 8084 | Register, login, JWT issuance                    |
| customer-service     | 8081 | Customer CRUD                                    |
| restaurant-service   | 8082 | Restaurant, menu item, and order management      |
| order-service        | 8080 | Place and track customer orders                  |
| delivery-service     | 8083 | Driver and delivery management                   |
| notification-service | 8087 | WhatsApp notifications via Twilio                |
| chat-service         | 8085 | AI chat (Google Gemini) with MCP tool access     |
| mcp-service          | 8090 | MCP server exposing order/customer/delivery data |
| discovery-server     | 8761 | Eureka service registry                          |

## Tech Stack

- **Java 21** · **Spring Boot 4.0.3** · **Spring Cloud 2025.1.1**
- **PostgreSQL 16** — one database per service
- **Apache Kafka** (KRaft mode, Confluent 7.7.0)
- **Eureka** for service discovery
- **Spring AI** + **Google Gemini** (`gemini-2.0-flash`) for the chat service
- **Twilio** for WhatsApp notifications
- **Docker Compose** for the full stack

## Getting Started

### Prerequisites

- Docker Desktop
- Java 21
- Maven 3.9+

### Environment Variables

Create a `.env` file in the project root:

```env
JWT_SECRET=<base64-encoded-secret-min-32-bytes>
GOOGLE_AI_API_KEY=<your-gemini-api-key>
TWILIO_ACCOUNT_SID=<your-twilio-sid>
TWILIO_AUTH_TOKEN=<your-twilio-auth-token>
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
```

### Run the Full Stack

```bash
docker compose up --build
```

### Run Infrastructure Only (for local development)

```bash
docker compose up zookeeper kafka postgres-customer postgres-restaurant postgres-order postgres-delivery postgres-auth postgres-notification discovery-server
```

## API Overview

All requests go through the gateway at `http://localhost:8088`.  
Use `/auth/register/customer` to register, then `/auth/login` to obtain a JWT, and include it as `Authorization: Bearer <token>`.

### Auth

| Method | Path                  | Auth        | Description                              |
|--------|-----------------------|-------------|-------------------------------------------|
| POST   | /auth/register/customer | No        | Register as customer (no JWT returned)   |
| POST   | /auth/register/staff  | Yes (admin) | Register staff (driver/restaurant/admin) |
| POST   | /auth/login           | No          | Login + get JWT                          |
| POST   | /auth/validate        | No          | Validate JWT                             |
| GET    | /auth/users           | Yes (SUPER_ADMIN) | List all user credentials          |

> **Planned refactor:** `/auth/login` currently issues a single long-lived JWT (24h) with no logout endpoint.
> This will change to short-lived access tokens + refresh tokens (with a `/auth/logout` endpoint to revoke
> the refresh token).

### Customers

| Method | Path                  | Auth | Description             |
|--------|-----------------------|------|-------------------------|
| POST   | /customers            | JWT  | Create customer         |
| GET    | /customers/{id}       | JWT  | Get customer by ID      |
| GET    | /customers?email=     | JWT  | Get customer by email   |
| PUT    | /customers/{id}       | JWT  | Update customer         |
| DELETE | /customers/{id}       | JWT  | Delete customer         |

### Restaurants

| Method | Path                                                | Auth | Description                   |
|--------|-----------------------------------------------------|------|-------------------------------|
| POST   | /restaurants                                        | JWT  | Create restaurant (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| GET    | /restaurants/{id}                                   | JWT  | Get restaurant by ID          |
| GET    | /restaurants?cuisineType=                           | JWT  | Filter by cuisine type        |
| GET    | /restaurants?isOpen=                                | JWT  | Filter by availability        |
| PUT    | /restaurants/{id}                                   | JWT  | Update restaurant (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| DELETE | /restaurants/{id}                                   | JWT  | Delete restaurant (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| POST   | /restaurants/{id}/menu-items                        | JWT  | Add menu item (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| GET    | /restaurants/{id}/menu-items                        | JWT  | List menu items               |
| GET    | /restaurants/{id}/menu-items/{itemId}               | JWT  | Get menu item                 |
| PUT    | /restaurants/{id}/menu-items/{itemId}               | JWT  | Update menu item (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| DELETE | /restaurants/{id}/menu-items/{itemId}               | JWT  | Delete menu item (SUPER_ADMIN/RESTAURANT_ADMIN only) |
| GET    | /restaurants/{id}/orders                            | JWT  | All orders for a restaurant; restaurant staff only (SUPER_ADMIN/RESTAURANT_ADMIN/RESTAURANT_EMPLOYEE) |
| PATCH  | /restaurants/{id}/orders/{orderId}/status           | JWT  | Update restaurant order status; restaurant staff only (SUPER_ADMIN/RESTAURANT_ADMIN/RESTAURANT_EMPLOYEE) |

`RESTAURANT_EMPLOYEE` may only view and update the status of restaurant orders — it gets `403` on all
restaurant/menu-item create/update/delete endpoints. Browsing restaurants and menu items (the `GET` endpoints
above) stays open to every authenticated role. See `PROJECT_CONTEXT.md` → `restaurant-service` →
"Authorization: RESTAURANT_EMPLOYEE role restrictions" for details.

> **Known gap:** `RESTAURANT_ADMIN`/`RESTAURANT_EMPLOYEE` accounts aren't scoped to a specific restaurant yet — any
> staff member with either role can act on any restaurant's data. Scoped out (not implemented) in
> `PROJECT_CONTEXT.md` → `restaurant-service` → "Design scope: staff-to-restaurant ownership".

### Orders

| Method | Path                          | Auth | Description              |
|--------|-------------------------------|------|--------------------------|
| POST   | /orders                       | JWT  | Place order              |
| GET    | /orders/{id}                  | JWT  | Get order by ID          |
| GET    | /orders/customer/{customerId} | JWT  | All orders for customer  |
| PATCH  | /orders/{id}/status           | JWT  | Update order status      |

### Deliveries & Drivers

| Method | Path                      | Auth | Description                                |
|--------|---------------------------|------|---------------------------------------------|
| GET    | /deliveries/{id}          | JWT  | Get delivery by ID                          |
| GET    | /deliveries?orderId=      | JWT  | Get delivery by order ID                    |
| GET    | /deliveries               | JWT  | List deliveries (optional ?status=); DELIVERY_DRIVER sees only its own |
| PATCH  | /deliveries/{id}/status   | JWT  | Update delivery status                      |
| GET    | /drivers                  | JWT  | List drivers (optional ?status=)  |
| GET    | /drivers/{id}             | JWT  | Get driver by ID                  |
| PATCH  | /drivers/{id}/status      | JWT  | Update driver status              |
| DELETE | /drivers/{id}             | JWT  | Delete driver                     |

### Chat

| Method | Path  | Auth | Description            |
|--------|-------|------|------------------------|
| POST   | /chat | JWT  | Chat with AI assistant |

## Chat UI

A browser-based chat interface is available at `http://localhost:8085` when the stack is running.  
Log in with a seed account (`anna.mueller@example.com` / `password123`) to start chatting.

## API Tester

`api-tester.html` in the project root is a browser-based UI covering all services.  
Open it locally, log in via the Auth tab to get a JWT, then explore every endpoint without Postman.

## Postman Collection

`FoodDeliveryService.postman_collection.json` in the project root has an `AuthService` folder with subfolders:

- **Superadmin** — login, creates one staff account per role (`restaurant_admin`, `restaurant_employee`,
  `delivery_admin`, `delivery_driver`), logout (client-side), then a post-logout validate check (expect 400)
- **Restaurant Admin** — login, creates a `restaurant_employee` account (scoped staff creation), logout (client-side)
- **Restaurant Employee** / **Delivery Driver** — login + logout (client-side) for the account created above
- **Delivery Admin** — login, creates a `delivery_driver` account (scoped staff creation), logout (client-side)

The scoped staff-creation requests exercise `AuthService.assertCanCreate`: a `RESTAURANT_ADMIN` may only create
`RESTAURANT_EMPLOYEE` accounts, and a `DELIVERY_ADMIN` may only create `DELIVERY_DRIVER` accounts.

It also has an `OrderService` folder that places a real order as the seeded customer Anna Müller and drives it
end-to-end — order-service → restaurant-service (advancing the restaurant order through its state machine) →
delivery-service — polling after each async step (Kafka via the outbox pattern) until the next service has
caught up. This produces a genuinely fresh delivery every run.

It also has a `DeliveryService` folder with subfolders:

- **Delivery Admin** — login, full `DeliveryController` + `DriverController` coverage (get by id, get by order id,
  list, status update, driver delete — each with a 404/409 negative case), logout (client-side)
- **Delivery Driver** — login, gets its own deliveries (scoped server-side), 403 checks confirming a driver can't
  read another driver's delivery or touch the `/drivers` roster, logout (client-side)

...and a `RestaurantService` folder with subfolders:

- **Restaurant Admin** — login, full create→read→update→delete cycle for both a restaurant and a menu item,
  logout (client-side); exercises `RestaurantService.assertCallerIsAdmin` allowing `RESTAURANT_ADMIN`
- **Restaurant Employee** — login, gets restaurant orders and updates a restaurant order's status (both allowed),
  403 checks confirming an employee can't create/update/delete a restaurant or menu item, gets all restaurants
  (browsing stays open), logout (client-side)
- **Customer** — login, 403 check confirming a customer can't hit the restaurant-orders endpoint

The `Restaurant Employee`/`Customer` 403 checks exercise `RestaurantOrderService.assertCallerManagesRestaurantOrders`
and `RestaurantService.assertCallerIsAdmin` (both throw `InsufficientRoleException` → 403).

The delivery-by-id/order-id/status-update requests need at least one existing delivery to act on. The `OrderService`
folder's last request captures the delivery it just created into collection variables for them to use; if you skip
that folder, "Delivery Admin Gets All Deliveries" falls back to capturing whatever it finds (e.g. the static fixture
in `delivery-service/data.sql`), and the dependent requests fail fast with a clear error if nothing exists at all.

Import it into Postman, set `superadminPassword` (and `baseUrl` if not using the gateway), then run the
**Superadmin** folder first (it creates the other accounts), followed by **OrderService**, then the remaining
folders in any order. This is also the order the folders are stored in, so a full "Run collection" works out of
the box.

## Seed Data

The following accounts are pre-loaded and ready to use:

| Name         | Email                      | Password    |
|--------------|----------------------------|-------------|
| Anna Müller  | anna.mueller@example.com   | password123 |
| Ben Schmidt  | ben.schmidt@example.com    | password123 |
| Clara Weber  | clara.weber@example.com    | password123 |
| Max Müller   | max.mueller@example.com    | password123 |
| Lisa Schmidt | lisa.schmidt@example.com   | password123 |
| Tom Wagner   | tom.wagner@example.com     | password123 |

Three restaurants (Bella Italia, Burger Palace, Tokyo Garden) and three drivers (Max, Lisa, Tom — Max and Lisa can
log in and act as `DELIVERY_DRIVER`) are also pre-loaded.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build a specific module
mvn clean install -pl order-service -am

# Skip tests
mvn clean package -DskipTests

# Run tests for one module
mvn test -pl customer-service
```
