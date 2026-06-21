package com.javatechdebasis.ecommerce.common;

/**
 * Lifecycle of an order as it moves through the distributed saga.
 */
public enum OrderStatus {
    /** Order persisted, saga started, awaiting inventory + payment. */
    PENDING,
    /** Inventory reserved and payment captured successfully. */
    CONFIRMED,
    /** Inventory or payment failed; compensating actions applied. */
    CANCELLED
}
