package com.javatechdebasis.ecommerce.order.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.InventoryFailedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentCompletedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Drives the order side of the saga by reacting to downstream events.
 *
 * <p>Class-level {@code @KafkaListener} + {@code @KafkaHandler} routes each
 * message to a method by its deserialized payload type (resolved from the
 * Kafka type header), so a single listener can subscribe to topics that carry
 * multiple event types and ignore the ones it does not care about.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {KafkaTopics.INVENTORY_EVENTS, KafkaTopics.PAYMENT_EVENTS}, groupId = "order-service")
public class OrderSagaListener {

    private final OrderService orderService;

    /** Inventory could not be reserved -> cancel the order. */
    @KafkaHandler
    public void on(InventoryFailedEvent event) {
        log.info("Received InventoryFailedEvent for orderId={}", event.getOrderId());
        orderService.cancelOrder(event.getOrderId(), "Inventory reservation failed: " + event.getReason());
    }

    /** Payment captured -> confirm the order. */
    @KafkaHandler
    public void on(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for orderId={}", event.getOrderId());
        orderService.confirmOrder(event.getOrderId());
    }

    /** Payment failed -> cancel the order (inventory is released by inventory-service). */
    @KafkaHandler
    public void on(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for orderId={}", event.getOrderId());
        orderService.cancelOrder(event.getOrderId(), "Payment failed: " + event.getReason());
    }

    /** Events this service does not act on (e.g. InventoryReservedEvent). */
    @KafkaHandler(isDefault = true)
    public void onOther(Object event) {
        log.debug("Ignoring event of type {}", event.getClass().getSimpleName());
    }
}
