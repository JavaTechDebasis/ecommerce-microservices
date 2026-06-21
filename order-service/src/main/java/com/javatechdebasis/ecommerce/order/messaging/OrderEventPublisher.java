package com.javatechdebasis.ecommerce.order.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order domain events to Kafka. The order id is used as the message
 * key so that all events for the same order land on the same partition and are
 * processed in order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
