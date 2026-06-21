# Sequence Diagrams

These diagrams use [Mermaid](https://mermaid.js.org/), which renders automatically on GitHub. Each diagram shows one flow through the choreography saga.

---

## 1. Happy Path — Order Confirmed

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway
    participant O as order-service
    participant K as Kafka
    participant I as inventory-service
    participant P as payment-service
    participant N as notification-service

    C->>GW: POST /api/orders
    GW->>O: route /api/orders
    O->>O: save Order (PENDING)
    O-->>C: 201 Created (PENDING)
    O->>K: publish OrderCreatedEvent (order-events)

    K->>I: OrderCreatedEvent
    I->>I: reserve stock (availableQty - reservedQty >= qty)
    I->>K: publish InventoryReservedEvent (inventory-events)

    K->>P: InventoryReservedEvent
    P->>P: amount <= approval-limit -> charge
    P->>P: save Payment (COMPLETED)
    P->>K: publish PaymentCompletedEvent (payment-events)

    K->>O: PaymentCompletedEvent
    O->>O: update Order -> CONFIRMED

    K->>N: PaymentCompletedEvent
    N->>N: save + send "order confirmed" notification
```

---

## 2. Payment Failure — Order Cancelled + Compensation

The amount exceeds the payment approval limit, so payment fails. The order is cancelled **and** the inventory previously reserved is released (the compensating action).

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway
    participant O as order-service
    participant K as Kafka
    participant I as inventory-service
    participant P as payment-service
    participant N as notification-service

    C->>GW: POST /api/orders (amount > limit)
    GW->>O: route /api/orders
    O->>O: save Order (PENDING)
    O-->>C: 201 Created (PENDING)
    O->>K: OrderCreatedEvent

    K->>I: OrderCreatedEvent
    I->>I: reserve stock
    I->>K: InventoryReservedEvent

    K->>P: InventoryReservedEvent
    P->>P: amount > approval-limit -> reject
    P->>P: save Payment (FAILED)
    P->>K: PaymentFailedEvent

    par Order side
        K->>O: PaymentFailedEvent
        O->>O: update Order -> CANCELLED
    and Compensation
        K->>I: PaymentFailedEvent
        I->>I: release reserved stock
    and Notify
        K->>N: PaymentFailedEvent
        N->>N: send "order cancelled (payment failed)"
    end
```

---

## 3. Out of Stock — Order Cancelled Early

Inventory reservation fails immediately, so the saga short-circuits before payment.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway
    participant O as order-service
    participant K as Kafka
    participant I as inventory-service
    participant N as notification-service

    C->>GW: POST /api/orders (qty > stock)
    GW->>O: route /api/orders
    O->>O: save Order (PENDING)
    O-->>C: 201 Created (PENDING)
    O->>K: OrderCreatedEvent

    K->>I: OrderCreatedEvent
    I->>I: insufficient stock
    I->>K: InventoryFailedEvent

    par Order side
        K->>O: InventoryFailedEvent
        O->>O: update Order -> CANCELLED
    and Notify
        K->>N: InventoryFailedEvent
        N->>N: send "order cancelled (out of stock)"
    end
    Note over P: payment-service is never involved
```

---

## 4. State Machine (Order)

```mermaid
stateDiagram-v2
    [*] --> PENDING: createOrder()
    PENDING --> CANCELLED: InventoryFailedEvent
    PENDING --> CANCELLED: PaymentFailedEvent
    PENDING --> CONFIRMED: PaymentCompletedEvent
    CONFIRMED --> [*]
    CANCELLED --> [*]
```
