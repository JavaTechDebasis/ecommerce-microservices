package com.javatechdebasis.ecommerce.payment.repository;

import com.javatechdebasis.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long orderId);
}
