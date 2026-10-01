# Initial System Architecture

The platform is a set of independently deployable Spring Boot services. Services communicate synchronously through the API gateway or service clients where a direct response is required, and asynchronously through Kafka for domain events. No domain entity or database is shared between services.

## Responsibilities

- **API Gateway**: routes external traffic, applies OAuth2 resource-server authentication, and exposes the platform edge.
- **Order Service**: owns order creation and lifecycle transitions and publishes order events.
- **Customer Service**: owns customer identity-related delivery data and preferences.
- **Driver Service**: owns driver profiles, status, and availability.
- **Location Service**: stores and serves current driver location with Redis and publishes location updates.
- **Dispatch Service**: coordinates assignment decisions using driver and order events, with Redis for fast dispatch state.
- **Tracking Service**: consumes lifecycle and location events and provides real-time tracking over WebSocket.
- **Notification Service**: consumes notification-worthy events and coordinates delivery notifications.

## Data and messaging

PostgreSQL is used by order, customer, driver, and notification services. Each service must use a separate database/schema in deployed environments. Redis supports low-latency location and dispatch state. Kafka is the event backbone; event envelopes are versioned under `common/event-schemas`.

The gateway is the only public entry point in the initial deployment. Actuator health endpoints are enabled for orchestration probes and later monitoring integration.
