package com.delivery.order.application.service;

import com.delivery.order.application.dto.CreateOrderRequest;
import com.delivery.order.application.dto.OrderResponse;
import com.delivery.order.domain.event.OrderCreatedEvent;
import com.delivery.order.domain.exception.OrderNotFoundException;
import com.delivery.order.domain.model.Order;
import com.delivery.order.domain.model.OrderStatus;
import com.delivery.order.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final Clock clock;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this(orderRepository, kafkaTemplate, Clock.systemUTC());
    }

    OrderService(OrderRepository orderRepository,
                 KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                 Clock clock) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(
                UUID.randomUUID(),
                request.customerId(),
                request.pickupAddress().trim(),
                request.deliveryAddress().trim(),
                OrderStatus.CREATED,
                Instant.now(clock)
        );

        Order savedOrder = orderRepository.save(order);
        OrderCreatedEvent event = OrderCreatedEvent.from(UUID.randomUUID(), savedOrder);
        kafkaTemplate.send(OrderCreatedEvent.TOPIC, savedOrder.getId().toString(), event);

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponse::from);
    }
}
