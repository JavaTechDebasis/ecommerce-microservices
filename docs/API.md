# API Reference

All client traffic goes through the **API Gateway** at `http://localhost:8080`.
Individual services can also be hit directly on their own ports (useful for debugging).

| Resource | Via Gateway | Direct |
|----------|-------------|--------|
| Orders | `http://localhost:8080/api/orders` | `http://localhost:8081/api/orders` |
| Inventory | `http://localhost:8080/api/inventory` | `http://localhost:8082/api/inventory` |
| Payments | `http://localhost:8080/api/payments` | `http://localhost:8083/api/payments` |
| Notifications | — | `http://localhost:8084/api/notifications` |

---

## Order Service

### Create an order
`POST /api/orders`

Request body:
```json
{
  "productCode": "LAPTOP-001",
  "quantity": 1,
  "amount": 999,
  "customerEmail": "alice@example.com"
}
```

| Field | Type | Rules |
|-------|------|-------|
| `productCode` | string | required, not blank |
| `quantity` | int | required, >= 1 |
| `amount` | number | required, > 0 |
| `customerEmail` | string | required, valid email |

Response `201 Created` (order starts in `PENDING`; the saga then moves it to `CONFIRMED`/`CANCELLED` asynchronously):
```json
{
  "id": 1,
  "productCode": "LAPTOP-001",
  "quantity": 1,
  "amount": 999.00,
  "customerEmail": "alice@example.com",
  "status": "PENDING",
  "statusReason": null,
  "createdAt": "2026-06-21T14:35:43.521Z",
  "updatedAt": "2026-06-21T14:35:43.521Z"
}
```

Validation error `400 Bad Request`:
```json
{
  "timestamp": "2026-06-21T14:40:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": { "customerEmail": "customerEmail must be a valid email" }
}
```

### Get an order
`GET /api/orders/{id}` → `200 OK` (or `404 Not Found`)

### List orders
`GET /api/orders` → `200 OK` (array)

`status` is one of `PENDING`, `CONFIRMED`, `CANCELLED`; `statusReason` explains a cancellation.

---

## Inventory Service

### List products / current stock
`GET /api/inventory` → `200 OK`
```json
[
  {
    "id": 1, "productCode": "LAPTOP-001", "name": "14-inch Laptop",
    "price": 999.00, "availableQuantity": 10, "reservedQuantity": 1, "version": 3
  }
]
```
Seeded catalogue: `LAPTOP-001` (10), `PHONE-001` (25), `HEADSET-001` (3).

---

## Payment Service

### List payments
`GET /api/payments` → `200 OK`
```json
[
  {
    "id": 1, "orderId": 1,
    "paymentReference": "PAY-59677b39-...", "amount": 999.00,
    "customerEmail": "alice@example.com", "status": "COMPLETED", "failureReason": null,
    "createdAt": "2026-06-21T14:35:44Z"
  }
]
```
`status` is `COMPLETED` or `FAILED`. Orders above the configured **approval limit** (`payment.approval-limit`, default `5000`) fail.

---

## Notification Service

### List notifications
`GET /api/notifications` → `200 OK`
```json
[
  {
    "id": 2, "orderId": 1, "recipient": "alice@example.com", "channel": "EMAIL",
    "message": "Your order #1 is confirmed. Payment PAY-... of 999 was successful.",
    "sentAt": "2026-06-21T14:35:44Z"
  }
]
```

---

## Actuator
Every service exposes `GET /actuator/health` (and `/info`). The gateway also exposes `/actuator/gateway`.

---

## Example: full happy-path with curl
```bash
# place an order
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productCode":"LAPTOP-001","quantity":1,"amount":999,"customerEmail":"alice@example.com"}'

# a moment later, the order is CONFIRMED
curl http://localhost:8080/api/orders
curl http://localhost:8080/api/payments
curl http://localhost:8084/api/notifications
```
