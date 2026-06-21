package com.javatechdebasis.ecommerce.inventory.repository;

import com.javatechdebasis.ecommerce.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);
}
