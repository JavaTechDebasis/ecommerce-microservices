package com.javatechdebasis.ecommerce.order.service;

import com.javatechdebasis.ecommerce.common.OrderStatus;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.order.dto.CreateOrderRequest;
import com.javatechdebasis.ecommerce.order.dto.OrderResponse;
import com.javatechdebasis.ecommerce.order.entity.OrderEntity;
import com.javatechdebasis.ecommerce.order.exception.OrderNotFoundException;
import com.javatechdebasis.ecommerce.order.messaging.OrderEventPublisher;
import com.javatechdebasis.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    /**
     * Persists a new order in PENDING state and starts the saga by emitting an
     * {@link OrderCreatedEvent}.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        OrderEntity order = OrderEntity.builder()
                .productCode(request.getProductCode())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        OrderEntity saved = orderRepository.save(order);
        log.info("Created order id={} status=PENDING", saved.getId());

        eventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .productCode(saved.getProductCode())
                .quantity(saved.getQuantity())
                .amount(saved.getAmount())
                .customerEmail(saved.getCustomerEmail())
                .build());

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        OrderEntity order = findById(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStatusReason("Inventory reserved and payment captured");
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        log.info("Order id={} -> CONFIRMED", orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        OrderEntity order = findById(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusReason(reason);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        log.info("Order id={} -> CANCELLED ({})", orderId, reason);
    }

    private OrderEntity findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
