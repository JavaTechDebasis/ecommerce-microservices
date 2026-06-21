package com.javatechdebasis.ecommerce.payment.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.PaymentCompletedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, String.valueOf(event.getOrderId()), event);
    }

    public void publishFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
