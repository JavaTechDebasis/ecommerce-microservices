package com.javatechdebasis.ecommerce.inventory.service;

import com.javatechdebasis.ecommerce.common.event.InventoryFailedEvent;
import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.inventory.entity.Product;
import com.javatechdebasis.ecommerce.inventory.messaging.InventoryEventPublisher;
import com.javatechdebasis.ecommerce.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product(int available, int reserved) {
        return Product.builder()
                .id(1L)
                .productCode("LAPTOP-001")
                .name("14-inch Laptop")
                .price(new BigDecimal("999.00"))
                .availableQuantity(available)
                .reservedQuantity(reserved)
                .build();
    }

    private OrderCreatedEvent orderEvent(int quantity) {
        return OrderCreatedEvent.builder()
                .orderId(1L)
                .productCode("LAPTOP-001")
                .quantity(quantity)
                .amount(new BigDecimal("999.00"))
                .customerEmail("alice@example.com")
                .build();
    }

    @Test
    void reserve_whenEnoughStock_reservesAndPublishesReserved() {
        Product product = product(10, 0);
        when(productRepository.findByProductCode("LAPTOP-001")).thenReturn(Optional.of(product));

        inventoryService.reserve(orderEvent(2));

        assertThat(product.getReservedQuantity()).isEqualTo(2);
        verify(productRepository).save(product);
        verify(eventPublisher).publishReserved(any(InventoryReservedEvent.class));
        verify(eventPublisher, never()).publishFailed(any());
    }

    @Test
    void reserve_whenInsufficientStock_publishesFailed() {
        when(productRepository.findByProductCode("LAPTOP-001")).thenReturn(Optional.of(product(3, 0)));

        inventoryService.reserve(orderEvent(10));

        verify(eventPublisher).publishFailed(any(InventoryFailedEvent.class));
        verify(eventPublisher, never()).publishReserved(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void reserve_whenUnknownProduct_publishesFailed() {
        when(productRepository.findByProductCode("LAPTOP-001")).thenReturn(Optional.empty());

        inventoryService.reserve(orderEvent(1));

        verify(eventPublisher).publishFailed(any(InventoryFailedEvent.class));
        verify(eventPublisher, never()).publishReserved(any());
    }

    @Test
    void release_reducesReservedQuantity() {
        Product product = product(10, 5);
        when(productRepository.findByProductCode("LAPTOP-001")).thenReturn(Optional.of(product));

        inventoryService.release(PaymentFailedEvent.builder()
                .orderId(1L)
                .productCode("LAPTOP-001")
                .quantity(3)
                .customerEmail("alice@example.com")
                .reason("Payment failed")
                .build());

        assertThat(product.getReservedQuantity()).isEqualTo(2);
        verify(productRepository).save(product);
    }
}
