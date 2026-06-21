package com.javatechdebasis.ecommerce.order.repository;

import com.javatechdebasis.ecommerce.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
