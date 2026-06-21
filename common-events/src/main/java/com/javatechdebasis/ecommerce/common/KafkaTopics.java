package com.javatechdebasis.ecommerce.common;

/**
 * Central registry of Kafka topic names used by the choreography saga.
 * Keeping topic names in one shared place avoids typos and keeps producers
 * and consumers in agreement across services.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    /** Published by order-service when a new order is created. */
    public static final String ORDER_EVENTS = "order-events";

    /** Published by inventory-service after a reserve / reserve-failed attempt. */
    public static final String INVENTORY_EVENTS = "inventory-events";

    /** Published by payment-service after a payment success / failure. */
    public static final String PAYMENT_EVENTS = "payment-events";
}
