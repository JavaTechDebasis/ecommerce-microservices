package com.javatechdebasis.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Emitted by payment-service on {@code payment-events} after a successful charge.
 * Consumed by order-service (confirm) and notification-service (notify customer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String paymentReference;
    private BigDecimal amount;
    private String customerEmail;
}
