# Realtime Delivery Platform

`realtime-delivery-platform` is a Java 21 and Spring Boot monorepo for a real-time delivery platform. Each service is independently buildable and deployable, owns its domain model, and communicates through explicit APIs and versioned Kafka events.

## Architecture

The API gateway is the external entry point. Business services own their data and responsibilities:

- PostgreSQL is used by services that need relational persistence.
- Redis supports low-latency location and dispatch state.
- Kafka provides asynchronous communication between services.
- Actuator exposes health and operational endpoints for every service.
- Docker Compose supplies local infrastructure only; application services are not included yet.

See [docs/architecture/architecture.md](docs/architecture/architecture.md) for the initial architecture and service responsibilities.

## Services

| Service | Responsibility | Port |
| --- | --- | ---: |
| `api-gateway` | Routing, edge security, and OAuth2 resource-server integration | 8080 |
| `order-service` | Order lifecycle and order events | 8081 |
| `customer-service` | Customer profiles and preferences | 8082 |
| `driver-service` | Driver profiles, availability, and status | 8083 |
| `location-service` | Current driver locations | 8084 |
| `dispatch-service` | Assignment and dispatch coordination | 8085 |
| `tracking-service` | Real-time tracking over WebSocket | 8086 |
| `notification-service` | Notification event handling | 8087 |

## Technology stack

- Java 21
- Maven 3.9+
- Spring Boot 3.3.x
- Spring Cloud Gateway
- Spring Security and OAuth2 Resource Server
- Spring Web and WebSocket
- Spring Data JPA and PostgreSQL
- Spring Kafka
- Spring Data Redis
- Flyway
- Spring Boot Actuator
- Docker Compose
- Planned: Kubernetes, Helm, Terraform, Prometheus, Grafana, and OpenTelemetry

## Prerequisites

- JDK 21
- Maven 3.9 or newer
- Docker Desktop or Docker Engine with Compose v2

Verify the local tools:

```bash
java -version
mvn -version
docker compose version
```

## Start local infrastructure

Start PostgreSQL, Redis, and Kafka in KRaft mode:

```bash
docker compose up -d
```

Infrastructure endpoints:

| Component | Address |
| --- | --- |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |

The Compose defaults are intended for local development:

```text
PostgreSQL database: delivery
PostgreSQL username: delivery
PostgreSQL password: delivery
```

Override them with environment variables when needed:

```bash
POSTGRES_USER=delivery \
POSTGRES_PASSWORD=change-me \
POSTGRES_DB=delivery \
docker compose up -d
```

Stop infrastructure and remove its containers:

```bash
docker compose down
```

Remove containers and local data volumes:

```bash
docker compose down -v
```

## Build the services

Build every Maven module from the repository root:

```bash
mvn clean verify
```

Compile without running tests:

```bash
mvn -DskipTests compile
```

Build one service independently:

```bash
cd order-service
mvn clean verify
```

Each service can be packaged into a container after building:

```bash
mvn package -DskipTests
docker build -t realtime-delivery/order-service .
```

## Configuration

Database-backed services read credentials from environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Kafka defaults to `localhost:9092` and can be overridden with:

```text
KAFKA_BOOTSTRAP_SERVERS
```

Redis defaults to `localhost:6379` and can be overridden with:

```text
REDIS_HOST
REDIS_PORT
```

Do not commit `.env` files or credentials. Service configuration is stored in each service's `src/main/resources/application.yml`.

## Health checks

Every service exposes:

```text
GET http://localhost:<port>/actuator/health
```

For example:

```bash
curl http://localhost:8081/actuator/health
```

## Event schemas

Initial JSON schemas are stored in [`common/event-schemas`](common/event-schemas):

- `order-created.json`
- `order-cancelled.json`
- `driver-assigned.json`
- `driver-location-updated.json`
- `order-picked-up.json`
- `order-delivered.json`

Schemas share an event envelope containing `eventId`, `eventType`, `eventVersion`, `timestamp`, and `correlationId`.

## Planned Kafka topics

- `delivery.order.created`
- `delivery.order.cancelled`
- `delivery.driver.assigned`
- `delivery.driver.location`
- `delivery.driver.status`
- `delivery.order.picked-up`
- `delivery.order.delivered`
- `delivery.notification.requested`

## Repository layout

```text
api-gateway/          Edge gateway service
order-service/        Order bounded context
customer-service/     Customer bounded context
driver-service/       Driver bounded context
location-service/     Location and geospatial state
dispatch-service/     Dispatch coordination
tracking-service/     WebSocket tracking
notification-service/ Notification processing
common/event-schemas/ Versioned event contracts
infrastructure/       Docker, Kubernetes, Helm, and Terraform
monitoring/            Prometheus, Grafana, and OpenTelemetry
docs/                  Architecture, API, and diagram documentation
```

## Architecture diagram

The architecture diagram will be added under [`docs/diagrams`](docs/diagrams). The written architecture overview is available at [docs/architecture/architecture.md](docs/architecture/architecture.md).

## Roadmap

1. Finalize API contracts and event schema governance.
2. Add service-owned persistence migrations and domain models.
3. Implement order, customer, driver, dispatch, and tracking workflows.
4. Configure gateway routes and authentication integration.
5. Add observability dashboards, alerts, and distributed tracing.
6. Add Kubernetes manifests, Helm charts, and Terraform modules.
7. Add integration, contract, and end-to-end test suites.

## Project status

The repository currently contains production-style scaffolding only. Business workflows, CRUD endpoints, shared entities, and application deployment definitions are intentionally not implemented yet.
