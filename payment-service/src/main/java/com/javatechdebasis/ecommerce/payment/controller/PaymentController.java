package com.javatechdebasis.ecommerce.payment.controller;

import com.javatechdebasis.ecommerce.payment.entity.Payment;
import com.javatechdebasis.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<Payment> getAll() {
        return paymentService.findAll();
    }
}
