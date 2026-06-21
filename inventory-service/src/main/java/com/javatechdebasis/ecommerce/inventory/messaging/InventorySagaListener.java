package com.javatechdebasis.ecommerce.inventory.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {KafkaTopics.ORDER_EVENTS, KafkaTopics.PAYMENT_EVENTS}, groupId = "inventory-service")
public class InventorySagaListener {

    private final InventoryService inventoryService;

    /** New order -> try to reserve stock. */
    @KafkaHandler
    public void on(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId={}", event.getOrderId());
        inventoryService.reserve(event);
    }

    /** Payment failed -> release the stock reserved earlier (compensation). */
    @KafkaHandler
    public void on(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for orderId={}", event.getOrderId());
        inventoryService.release(event);
    }

    @KafkaHandler(isDefault = true)
    public void onOther(Object event) {
        log.debug("Ignoring event of type {}", event.getClass().getSimpleName());
    }
}
