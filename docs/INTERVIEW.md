# Interview Guide

This file helps you **present this project confidently** and answer the questions interviewers commonly ask about microservices, Kafka, and Spring Boot. Read the [`ARCHITECTURE.md`](ARCHITECTURE.md) and [`SEQUENCE_DIAGRAM.md`](SEQUENCE_DIAGRAM.md) alongside this.

---

## 1. 30-Second Pitch

> "I built an **event-driven E-Commerce Order Management System** using Java 8, Spring Boot, Spring Cloud, and Apache Kafka. It's split into independent microservices — order, inventory, payment, and notification — behind an API Gateway with Eureka service discovery. The order workflow is implemented as a **Saga using Kafka choreography**: when an order is placed, inventory is reserved, payment is captured, and the customer is notified, all through asynchronous events. If payment fails, a **compensating transaction** automatically releases the reserved stock and cancels the order. Each service has its own database, so they're fully decoupled."

---

## 2. Why each technology (be ready to justify)

| Tech | Why I used it |
|------|---------------|
| **Microservices** | Independent deployment, scaling, and failure isolation; each team/service owns a bounded context. |
| **Kafka** | Durable, ordered, replayable event log; decouples services in time (a down service just catches up later). |
| **Saga pattern** | Maintains data consistency across services **without** distributed transactions / 2-phase commit. |
| **Eureka** | Client-side service discovery so services find each other by name, not hard-coded URLs. |
| **API Gateway** | Single entry point: routing, and a natural place for cross-cutting concerns (auth, rate limiting, logging). |
| **Database per service** | Enforces autonomy; no shared schema means services can evolve independently. |
| **H2 / MySQL profile** | Runs anywhere instantly, but production DB is one flag away. |

---

## 3. The Saga, explained simply

There are two ways to coordinate a multi-service transaction:

- **Orchestration** — a central "orchestrator" tells each service what to do.
- **Choreography** — each service listens for events and reacts, emitting new events. *(This project uses choreography.)*

**Flow:** `OrderCreated → InventoryReserved → PaymentCompleted → Order CONFIRMED`.
On failure, **compensating events** undo prior steps: e.g. `PaymentFailed → inventory releases stock → Order CANCELLED`.

**Why choreography here?** The workflow is short and linear, so a central orchestrator would be overkill. I can clearly articulate the trade-off (see Q&A below), which is what interviewers want.

---

## 4. Likely Questions & Strong Answers

**Q: What is a Saga and why not just use a distributed transaction (2PC)?**
A: A Saga breaks one distributed transaction into a sequence of **local** transactions, each publishing an event that triggers the next. If a step fails, earlier steps are undone with **compensating transactions**. 2PC needs a coordinator and distributed locks that hurt availability and don't scale well; sagas keep services autonomous and highly available at the cost of only **eventual** consistency.

**Q: Choreography vs orchestration — which did you use and why?**
A: Choreography — services react to events with no central coordinator. It's simpler for a short, linear flow and keeps services decoupled. The downside is the end-to-end flow is implicit (spread across services), so for large, branching workflows I'd switch to orchestration for clarity and easier monitoring.

**Q: How do you guarantee ordering of events?**
A: I key every event by `orderId`. Kafka guarantees ordering **within a partition**, and a given key always maps to the same partition, so all events for one order are processed in order.

**Q: Kafka delivers at-least-once — how do you handle duplicates?**
A: Idempotency. For example, `payment-service` checks `existsByOrderId` before charging, so a redelivered `InventoryReservedEvent` won't double-charge. Compensations are written to be safe to apply once.

**Q: What's a "poison pill" and how did you handle it?**
A: A message that always fails to deserialize/process and blocks the partition. I use `ErrorHandlingDeserializer` wrapping `JsonDeserializer`, so a bad message is logged/skipped instead of stalling the consumer. In production I'd route it to a **dead-letter topic**.

**Q: How do producer and consumer agree on the event format?**
A: A shared `common-events` module holds the event classes and topic names, so both sides compile against the same contract. The concrete type travels in the `__TypeId__` Kafka header, letting one listener handle multiple event types via `@KafkaHandler`.

**Q: "Save to DB and publish to Kafka" isn't atomic. What if the app crashes between them?**
A: Correct — that's the dual-write problem. The robust fix is the **Transactional Outbox** pattern: write the event to an `outbox` table in the same DB transaction, then a relay (e.g. Debezium CDC) publishes it to Kafka. I called this out as a planned extension.

**Q: How does service discovery work?**
A: Each service registers with **Eureka** on startup. The gateway and clients resolve a logical name (e.g. `lb://order-service`) to a live instance via Eureka, with client-side load balancing — no hard-coded hosts/ports.

**Q: How would you scale this?**
A: Run multiple instances of a service in the same Kafka **consumer group** — Kafka distributes partitions across instances so they share the load while preserving per-key ordering. Stateless services + database-per-service make horizontal scaling straightforward.

**Q: How do you handle a service being down?**
A: Because communication is asynchronous, events simply accumulate in Kafka and are processed when the service recovers (temporal decoupling). For any synchronous calls I'd add **Resilience4j** circuit breakers/retries.

**Q: How would you debug a request that spans services?**
A: Add **distributed tracing** (Micrometer Tracing + Zipkin/Jaeger) with a correlation/trace id propagated through Kafka headers, so I can see the full saga timeline.

**Q: Why Java 8 / Spring Boot 2.7?**
A: It mirrors a very common enterprise baseline. Spring Boot 2.7 is the last line that supports Java 8. I can also discuss migrating to Java 17 + Boot 3.x (Jakarta namespace change).

---

## 5. What to Demo Live
1. `docker compose up -d zookeeper kafka` then `./scripts/start-all.sh`.
2. Show the **Eureka dashboard** (http://localhost:8761) — all services registered.
3. Run `./scripts/demo.sh` and walk through the three flows:
   - Happy path → `CONFIRMED`
   - Payment over limit → `CANCELLED` + stock released (compensation)
   - Out of stock → `CANCELLED`
4. Open **Kafka UI** (http://localhost:8090) and show the messages on `order-events`, `inventory-events`, `payment-events`.
5. Point at the **logs** to show events flowing between services.

---

## 6. Honest Talking Points (shows maturity)
- Choreography is implicit; for bigger flows I'd add an orchestrator or a saga framework.
- The DB+Kafka write isn't atomic yet → outbox pattern is the fix.
- No auth on the gateway yet → would add JWT/OAuth2 at the edge.
- H2 in-memory for demos; production needs real per-service databases, schema migrations (Flyway/Liquibase), and a dead-letter strategy.

---

## 7. Resume Bullet Points (copy/adapt)
- Designed and built an **event-driven microservices** e-commerce order system (Java 8, Spring Boot, Spring Cloud, Apache Kafka) using the **Saga choreography** pattern with automatic **compensating transactions**.
- Implemented **database-per-service** with Spring Data JPA and asynchronous, ordered event processing keyed by order id for correctness under concurrency.
- Added **service discovery (Eureka)** and an **API Gateway (Spring Cloud Gateway)** as the single entry point for all client traffic.
- Hardened Kafka consumers with **idempotency**, **optimistic locking**, and **poison-pill handling** (`ErrorHandlingDeserializer`).
- Containerized infrastructure with **Docker Compose** (Kafka, Zookeeper, MySQL) and automated end-to-end demo scripts.
