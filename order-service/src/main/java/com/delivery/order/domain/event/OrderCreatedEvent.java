package com.delivery.order.domain.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant timestamp,
        String correlationId,
        UUID orderId,
        UUID customerId,
        String pickupAddress,
        String deliveryAddress
) {
    public static final String TOPIC = "delivery.order.created";

    public static OrderCreatedEvent from(UUID correlationId, com.delivery.order.domain.model.Order order) {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                TOPIC,
                1,
                order.getCreatedAt(),
                correlationId.toString(),
                order.getId(),
                order.getCustomerId(),
                order.getPickupAddress(),
                order.getDeliveryAddress()
        );
    }
}
