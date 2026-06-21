package com.javatechdebasis.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Emitted by order-service on {@code order-events} when a customer places an order.
 * Starts the choreography saga.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String productCode;
    private int quantity;
    private BigDecimal amount;
    private String customerEmail;
}
