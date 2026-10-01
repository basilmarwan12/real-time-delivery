package com.delivery.order.application.dto;

import com.delivery.order.domain.model.Order;
import com.delivery.order.domain.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        String pickupAddress,
        String deliveryAddress,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
