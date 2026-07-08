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
| POST   | /restaurants                                        | JWT  | Create restaurant             |
| GET    | /restaurants/{id}                                   | JWT  | Get restaurant by ID          |
| GET    | /restaurants?cuisineType=                           | JWT  | Filter by cuisine type        |
| GET    | /restaurants?isOpen=                                | JWT  | Filter by availability        |
| PUT    | /restaurants/{id}                                   | JWT  | Update restaurant             |
| DELETE | /restaurants/{id}                                   | JWT  | Delete restaurant             |
| POST   | /restaurants/{id}/menu-items                        | JWT  | Add menu item                 |
| GET    | /restaurants/{id}/menu-items                        | JWT  | List menu items               |
| GET    | /restaurants/{id}/menu-items/{itemId}               | JWT  | Get menu item                 |
| PUT    | /restaurants/{id}/menu-items/{itemId}               | JWT  | Update menu item              |
| DELETE | /restaurants/{id}/menu-items/{itemId}               | JWT  | Delete menu item              |
| GET    | /restaurants/{id}/orders                            | JWT  | All orders for a restaurant   |
| PATCH  | /restaurants/{id}/orders/{orderId}/status           | JWT  | Update restaurant order status|

### Orders

| Method | Path                          | Auth | Description              |
|--------|-------------------------------|------|--------------------------|
| POST   | /orders                       | JWT  | Place order              |
| GET    | /orders/{id}                  | JWT  | Get order by ID          |
| GET    | /orders/customer/{customerId} | JWT  | All orders for customer  |
| PATCH  | /orders/{id}/status           | JWT  | Update order status      |

### Deliveries & Drivers

| Method | Path                      | Auth | Description                       |
|--------|---------------------------|------|-----------------------------------|
| GET    | /deliveries/{id}          | JWT  | Get delivery by ID                |
| GET    | /deliveries?orderId=      | JWT  | Get delivery by order ID          |
| PATCH  | /deliveries/{id}/status   | JWT  | Update delivery status            |
| POST   | /drivers                  | JWT  | Register driver                   |
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

## Seed Data

The following accounts are pre-loaded and ready to use:

| Name         | Email                      | Password    |
|--------------|----------------------------|-------------|
| Anna Müller  | anna.mueller@example.com   | password123 |
| Ben Schmidt  | ben.schmidt@example.com    | password123 |
| Clara Weber  | clara.weber@example.com    | password123 |

Three restaurants (Bella Italia, Burger Palace, Tokyo Garden) and three drivers are also pre-loaded.

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
