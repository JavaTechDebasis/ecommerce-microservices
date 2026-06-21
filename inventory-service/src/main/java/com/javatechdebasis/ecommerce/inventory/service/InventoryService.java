package com.javatechdebasis.ecommerce.inventory.service;

import com.javatechdebasis.ecommerce.common.event.InventoryFailedEvent;
import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.inventory.entity.Product;
import com.javatechdebasis.ecommerce.inventory.messaging.InventoryEventPublisher;
import com.javatechdebasis.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Attempts to reserve stock for a newly created order. Emits either an
     * {@link InventoryReservedEvent} (success) or {@link InventoryFailedEvent}
     * (unknown product / out of stock).
     */
    @Transactional
    public void reserve(OrderCreatedEvent event) {
        Optional<Product> productOpt = productRepository.findByProductCode(event.getProductCode());

        if (!productOpt.isPresent()) {
            fail(event, "Unknown product: " + event.getProductCode());
            return;
        }

        Product product = productOpt.get();
        int free = product.getAvailableQuantity() - product.getReservedQuantity();
        if (free < event.getQuantity()) {
            fail(event, "Insufficient stock for " + event.getProductCode()
                    + " (requested=" + event.getQuantity() + ", available=" + free + ")");
            return;
        }

        product.setReservedQuantity(product.getReservedQuantity() + event.getQuantity());
        productRepository.save(product);
        log.info("Reserved {} unit(s) of {} for orderId={}",
                event.getQuantity(), event.getProductCode(), event.getOrderId());

        eventPublisher.publishReserved(InventoryReservedEvent.builder()
                .orderId(event.getOrderId())
                .productCode(event.getProductCode())
                .quantity(event.getQuantity())
                .amount(event.getAmount())
                .customerEmail(event.getCustomerEmail())
                .build());
    }

    /**
     * Compensating action: releases previously reserved stock when a later step
     * (payment) fails.
     */
    @Transactional
    public void release(PaymentFailedEvent event) {
        productRepository.findByProductCode(event.getProductCode()).ifPresent(product -> {
            int restored = Math.max(0, product.getReservedQuantity() - event.getQuantity());
            product.setReservedQuantity(restored);
            productRepository.save(product);
            log.info("Released {} unit(s) of {} (compensation) for orderId={}",
                    event.getQuantity(), event.getProductCode(), event.getOrderId());
        });
    }

    private void fail(OrderCreatedEvent event, String reason) {
        log.warn("Inventory reservation failed for orderId={}: {}", event.getOrderId(), reason);
        eventPublisher.publishFailed(InventoryFailedEvent.builder()
                .orderId(event.getOrderId())
                .productCode(event.getProductCode())
                .quantity(event.getQuantity())
                .customerEmail(event.getCustomerEmail())
                .reason(reason)
                .build());
    }
}
