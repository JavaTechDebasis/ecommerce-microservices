package com.javatechdebasis.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Emitted by inventory-service on {@code inventory-events} when stock cannot be
 * reserved (out of stock / unknown product). Consumed by order-service (cancel)
 * and notification-service (notify customer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String productCode;
    private int quantity;
    private String customerEmail;
    private String reason;
}
