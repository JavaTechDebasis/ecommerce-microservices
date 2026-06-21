package com.javatechdebasis.ecommerce.notification.messaging;

import com.javatechdebasis.ecommerce.common.KafkaTopics;
import com.javatechdebasis.ecommerce.common.event.InventoryFailedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentCompletedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {KafkaTopics.INVENTORY_EVENTS, KafkaTopics.PAYMENT_EVENTS}, groupId = "notification-service")
public class NotificationListener {

    private final NotificationService notificationService;

    @KafkaHandler
    public void on(PaymentCompletedEvent event) {
        notificationService.notifyCustomer(event.getOrderId(), event.getCustomerEmail(),
                "Your order #" + event.getOrderId() + " is confirmed. Payment " + event.getPaymentReference()
                        + " of " + event.getAmount() + " was successful.");
    }

    @KafkaHandler
    public void on(PaymentFailedEvent event) {
        notificationService.notifyCustomer(event.getOrderId(), event.getCustomerEmail(),
                "Your order #" + event.getOrderId() + " was cancelled. Payment failed: " + event.getReason());
    }

    @KafkaHandler
    public void on(InventoryFailedEvent event) {
        notificationService.notifyCustomer(event.getOrderId(), event.getCustomerEmail(),
                "Your order #" + event.getOrderId() + " was cancelled. " + event.getReason());
    }

    @KafkaHandler(isDefault = true)
    public void onOther(Object event) {
        log.debug("Ignoring event of type {}", event.getClass().getSimpleName());
    }
}
