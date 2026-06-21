package com.javatechdebasis.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Emitted by payment-service on {@code payment-events} when a charge fails.
 * Triggers compensation: order-service cancels the order and inventory-service
 * releases the previously reserved stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String productCode;
    private int quantity;
    private String customerEmail;
    private String reason;
}
