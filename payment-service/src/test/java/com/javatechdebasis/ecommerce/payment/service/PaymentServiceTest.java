package com.javatechdebasis.ecommerce.payment.service;

import com.javatechdebasis.ecommerce.common.event.InventoryReservedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentCompletedEvent;
import com.javatechdebasis.ecommerce.common.event.PaymentFailedEvent;
import com.javatechdebasis.ecommerce.payment.entity.Payment;
import com.javatechdebasis.ecommerce.payment.entity.PaymentStatus;
import com.javatechdebasis.ecommerce.payment.messaging.PaymentEventPublisher;
import com.javatechdebasis.ecommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "approvalLimit", new BigDecimal("5000"));
    }

    private InventoryReservedEvent reserved(BigDecimal amount) {
        return InventoryReservedEvent.builder()
                .orderId(1L)
                .productCode("LAPTOP-001")
                .quantity(1)
                .amount(amount)
                .customerEmail("alice@example.com")
                .build();
    }

    @Test
    void process_underLimit_completesPaymentAndPublishesCompleted() {
        when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.process(reserved(new BigDecimal("999.00")));

        verify(eventPublisher).publishCompleted(any(PaymentCompletedEvent.class));
        verify(eventPublisher, never()).publishFailed(any());
    }

    @Test
    void process_overLimit_failsPaymentAndPublishesFailed() {
        when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.process(reserved(new BigDecimal("6000.00")));

        verify(eventPublisher).publishFailed(any(PaymentFailedEvent.class));
        verify(eventPublisher, never()).publishCompleted(any());
    }

    @Test
    void process_savedPaymentStatusReflectsOutcome() {
        when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.process(reserved(new BigDecimal("100.00")));

        verify(paymentRepository).save(org.mockito.ArgumentMatchers.argThat(
                p -> p.getStatus() == PaymentStatus.COMPLETED));
    }

    @Test
    void process_whenAlreadyProcessed_isIdempotent() {
        when(paymentRepository.existsByOrderId(1L)).thenReturn(true);

        paymentService.process(reserved(new BigDecimal("999.00")));

        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishCompleted(any());
        verify(eventPublisher, never()).publishFailed(any());
    }
}
