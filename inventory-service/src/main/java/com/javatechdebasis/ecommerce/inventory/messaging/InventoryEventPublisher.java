package com.javatechdebasis.ecommerce.inventory.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.InventoryFailedEvent;
import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(InventoryReservedEvent event) {
        log.info("Publishing InventoryReservedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, String.valueOf(event.getOrderId()), event);
    }

    public void publishFailed(InventoryFailedEvent event) {
        log.info("Publishing InventoryFailedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.INVENTORY_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
