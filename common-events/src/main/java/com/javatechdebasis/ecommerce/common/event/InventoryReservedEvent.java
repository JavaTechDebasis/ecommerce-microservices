package com.javatechdebasis.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Emitted by inventory-service on {@code inventory-events} once stock has been
 * successfully reserved for an order. Consumed by payment-service to capture payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String productCode;
    private int quantity;
    private BigDecimal amount;
    private String customerEmail;
}
