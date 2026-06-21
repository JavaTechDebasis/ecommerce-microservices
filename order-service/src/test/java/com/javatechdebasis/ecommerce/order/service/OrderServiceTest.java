package com.javatechdebasis.ecommerce.order.service;

import com.javatechdebasis.ecommerce.common.OrderStatus;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.order.dto.CreateOrderRequest;
import com.javatechdebasis.ecommerce.order.dto.OrderResponse;
import com.javatechdebasis.ecommerce.order.entity.OrderEntity;
import com.javatechdebasis.ecommerce.order.exception.OrderNotFoundException;
import com.javatechdebasis.ecommerce.order.messaging.OrderEventPublisher;
import com.javatechdebasis.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest sampleRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductCode("LAPTOP-001");
        request.setQuantity(2);
        request.setAmount(new BigDecimal("1998.00"));
        request.setCustomerEmail("alice@example.com");
        return request;
    }

    @Test
    void createOrder_persistsPendingOrderAndPublishesEvent() {
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        OrderResponse response = orderService.createOrder(sampleRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getProductCode()).isEqualTo("LAPTOP-001");

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishOrderCreated(captor.capture());
        OrderCreatedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getQuantity()).isEqualTo(2);
        assertThat(event.getCustomerEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void confirmOrder_setsStatusConfirmed() {
        OrderEntity entity = existingOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(entity));

        orderService.confirmOrder(1L);

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(entity);
    }

    @Test
    void cancelOrder_setsStatusCancelledWithReason() {
        OrderEntity entity = existingOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(entity));

        orderService.cancelOrder(1L, "Payment failed");

        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(entity.getStatusReason()).isEqualTo("Payment failed");
        verify(orderRepository).save(entity);
    }

    @Test
    void getOrder_whenMissing_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
        verify(eventPublisher, never()).publishOrderCreated(any());
    }

    private OrderEntity existingOrder() {
        return OrderEntity.builder()
                .id(1L)
                .productCode("LAPTOP-001")
                .quantity(2)
                .amount(new BigDecimal("1998.00"))
                .customerEmail("alice@example.com")
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
