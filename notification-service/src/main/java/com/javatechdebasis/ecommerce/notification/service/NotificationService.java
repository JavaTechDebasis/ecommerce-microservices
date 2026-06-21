package com.javatechdebasis.ecommerce.notification.service;

import com.javatechdebasis.ecommerce.notification.entity.Notification;
import com.javatechdebasis.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Stand-in for an email/SMS provider. Persists each notification and logs it so
 * the outcome of the saga is observable end-to-end.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public void notifyCustomer(Long orderId, String recipient, String message) {
        Notification notification = notificationRepository.save(Notification.builder()
                .orderId(orderId)
                .recipient(recipient)
                .channel("EMAIL")
                .message(message)
                .sentAt(Instant.now())
                .build());
        log.info("[EMAIL -> {}] orderId={} : {}", recipient, orderId, message);
        log.debug("Persisted notification id={}", notification.getId());
    }
}
