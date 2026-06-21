package com.javatechdebasis.ecommerce.order.entity;

import com.javatechdebasis.ecommerce.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** Human-readable reason populated when an order is cancelled. */
    private String statusReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
}
