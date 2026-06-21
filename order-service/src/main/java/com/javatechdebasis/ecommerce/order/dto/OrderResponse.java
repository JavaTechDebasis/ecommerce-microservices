package com.javatechdebasis.ecommerce.order.dto;

import com.javatechdebasis.ecommerce.common.OrderStatus;
import com.javatechdebasis.ecommerce.order.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String productCode;
    private int quantity;
    private BigDecimal amount;
    private String customerEmail;
    private OrderStatus status;
    private String statusReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(OrderEntity entity) {
        return OrderResponse.builder()
                .id(entity.getId())
                .productCode(entity.getProductCode())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
                .customerEmail(entity.getCustomerEmail())
                .status(entity.getStatus())
                .statusReason(entity.getStatusReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
