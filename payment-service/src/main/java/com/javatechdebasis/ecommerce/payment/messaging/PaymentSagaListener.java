package com.javatechdebasis.ecommerce.payment.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = KafkaTopics.INVENTORY_EVENTS, groupId = "payment-service")
public class PaymentSagaListener {

    private final PaymentService paymentService;

    /** Inventory reserved -> capture payment. */
    @KafkaHandler
    public void on(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for orderId={}", event.getOrderId());
        paymentService.process(event);
    }

    @KafkaHandler(isDefault = true)
    public void onOther(Object event) {
        log.debug("Ignoring event of type {}", event.getClass().getSimpleName());
    }
}
