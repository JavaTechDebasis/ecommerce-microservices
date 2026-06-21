package com.javatechdebasis.ecommerce.order.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "productCode is required")
    private String productCode;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "customerEmail is required")
    @Email(message = "customerEmail must be a valid email")
    private String customerEmail;
}
