package com.delivery.order.application.service;

import com.delivery.order.application.dto.CreateOrderRequest;
import com.delivery.order.application.dto.OrderResponse;
import com.delivery.order.domain.event.OrderCreatedEvent;
import com.delivery.order.domain.model.Order;
import com.delivery.order.domain.model.OrderStatus;
import com.delivery.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Test
    void createsOrderPersistsItAndPublishesEvent() {
        Instant createdAt = Instant.parse("2026-10-01T12:00:00Z");
        OrderService service = new OrderService(
                orderRepository,
                kafkaTemplate,
                Clock.fixed(createdAt, ZoneOffset.UTC)
        );
        UUID customerId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(customerId, " Pickup ", " Delivery ");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.createOrder(request);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.pickupAddress()).isEqualTo("Pickup");
        assertThat(response.deliveryAddress()).isEqualTo("Delivery");
        assertThat(response.createdAt()).isEqualTo(createdAt);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaTemplate).send(
                eq(OrderCreatedEvent.TOPIC),
                eq(response.orderId().toString()),
                eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(response.orderId());
        assertThat(eventCaptor.getValue().customerId()).isEqualTo(customerId);
    }

    @Test
    void getsOrderById() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                orderId,
                UUID.randomUUID(),
                "Pickup",
                "Delivery",
                OrderStatus.CREATED,
                Instant.parse("2026-10-01T12:00:00Z")
        );
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
        OrderService service = new OrderService(orderRepository, kafkaTemplate);

        OrderResponse response = service.getOrderById(orderId);

        assertThat(response.orderId()).isEqualTo(orderId);
    }

    @Test
    void getsOrdersPage() {
        Order order = new Order(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pickup",
                "Delivery",
                OrderStatus.CREATED,
                Instant.parse("2026-10-01T12:00:00Z")
        );
        PageRequest pageable = PageRequest.of(0, 20);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(order), pageable, 1));
        OrderService service = new OrderService(orderRepository, kafkaTemplate);

        Page<OrderResponse> responses = service.getOrders(pageable);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().getFirst().orderId()).isEqualTo(order.getId());
        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getTotalPages()).isEqualTo(1);
    }
}
