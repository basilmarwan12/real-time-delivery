# Realtime Delivery Platform

`realtime-delivery-platform` is a production-oriented Java 21 and Spring Boot monorepo for real-time delivery workflows. Each business capability is independently deployable and owns its domain model and persistence.

## Planned architecture

The platform uses an API gateway at the edge, event-driven communication through Kafka, service-owned PostgreSQL databases where relational storage is needed, and Redis for low-latency state such as locations and dispatch data. See [docs/architecture/architecture.md](docs/architecture/architecture.md).

## Microservices

- `api-gateway` - edge routing and OAuth2 resource-server security
- `order-service` - order lifecycle
- `customer-service` - customer profiles and preferences
- `driver-service` - driver profiles and availability
- `location-service` - current driver location
- `dispatch-service` - assignment and dispatch coordination
- `tracking-service` - real-time order tracking over WebSocket
- `notification-service` - delivery notifications

## Main technologies

Java 21, Maven, Spring Boot 3.3, Spring Cloud Gateway, Spring Security, OAuth2 Resource Server, Spring Web, WebSocket, Spring Data JPA, PostgreSQL, Spring Kafka, Spring Data Redis, Flyway, Actuator, Docker, Kubernetes, Helm, Terraform, Prometheus, Grafana, and OpenTelemetry.

## Local development prerequisites

- JDK 21
- Maven 3.9+
- Docker Desktop or Docker Engine with Compose v2

Each service has its own Maven project and can be built from its directory. The root Maven project also aggregates all services for a full compile.

## Start local infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432`, Redis on `localhost:6379`, and single-node Kafka in KRaft mode on `localhost:9092`. Configure each database service with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

## Architecture diagram

An architecture diagram will be added under `docs/diagrams/`.

## Planned Kafka topics

- `delivery.order.created`
- `delivery.order.cancelled`
- `delivery.driver.assigned`
- `delivery.driver.location`
- `delivery.driver.status`
- `delivery.order.picked-up`
- `delivery.order.delivered`
- `delivery.notification.requested`

## Project roadmap

1. Establish service contracts and event schemas.
2. Add persistence migrations and domain models per service.
3. Implement order, customer, driver, dispatch, and tracking workflows.
4. Add authentication integration and gateway routing.
5. Add observability dashboards, alerts, and distributed tracing.
6. Package services for Kubernetes deployment with Helm and Terraform.
