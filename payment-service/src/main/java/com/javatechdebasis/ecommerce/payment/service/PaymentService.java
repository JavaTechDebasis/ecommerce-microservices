package com.javatechdebasis.ecommerce.payment.service;

import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentCompletedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.payment.entity.Payment;
import com.javatechdebasis.ecommerce.payment.entity.PaymentStatus;
import com.javatechdebasis.ecommerce.payment.messaging.PaymentEventPublisher;
import com.javatechdebasis.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Simulated payment gateway limit. Orders above this amount are rejected so
     * the failure / compensation path of the saga can be demonstrated.
     */
    @Value("${payment.approval-limit:5000}")
    private BigDecimal approvalLimit;

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /**
     * Captures payment for an order whose stock has been reserved. Idempotent:
     * a duplicate event for an already-processed order is ignored.
     */
    @Transactional
    public void process(InventoryReservedEvent event) {
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.info("Payment for orderId={} already processed; skipping", event.getOrderId());
            return;
        }

        if (event.getAmount().compareTo(approvalLimit) > 0) {
            String reason = "Amount " + event.getAmount() + " exceeds approval limit " + approvalLimit;
            paymentRepository.save(Payment.builder()
                    .orderId(event.getOrderId())
                    .paymentReference("PAY-" + UUID.randomUUID())
                    .amount(event.getAmount())
                    .customerEmail(event.getCustomerEmail())
                    .status(PaymentStatus.FAILED)
                    .failureReason(reason)
                    .createdAt(Instant.now())
                    .build());
            log.warn("Payment FAILED for orderId={}: {}", event.getOrderId(), reason);

            eventPublisher.publishFailed(PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .productCode(event.getProductCode())
                    .quantity(event.getQuantity())
                    .customerEmail(event.getCustomerEmail())
                    .reason(reason)
                    .build());
            return;
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .orderId(event.getOrderId())
                .paymentReference("PAY-" + UUID.randomUUID())
                .amount(event.getAmount())
                .customerEmail(event.getCustomerEmail())
                .status(PaymentStatus.COMPLETED)
                .createdAt(Instant.now())
                .build());
        log.info("Payment COMPLETED for orderId={} ref={}", event.getOrderId(), payment.getPaymentReference());

        eventPublisher.publishCompleted(PaymentCompletedEvent.builder()
                .orderId(event.getOrderId())
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .customerEmail(payment.getCustomerEmail())
                .build());
    }
}
