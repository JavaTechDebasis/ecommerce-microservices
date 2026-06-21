package com.javatechdebasis.ecommerce.inventory.config;

import com.javatechdebasis.ecommerce.inventory.entity.Product;
import com.javatechdebasis.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Seeds a small product catalogue on startup so the saga can be demoed
 * immediately against the in-memory database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(Arrays.asList(
                Product.builder().productCode("LAPTOP-001").name("14-inch Laptop")
                        .price(new BigDecimal("999.00")).availableQuantity(10).reservedQuantity(0).build(),
                Product.builder().productCode("PHONE-001").name("Smartphone")
                        .price(new BigDecimal("499.00")).availableQuantity(25).reservedQuantity(0).build(),
                Product.builder().productCode("HEADSET-001").name("Wireless Headset")
                        .price(new BigDecimal("149.00")).availableQuantity(3).reservedQuantity(0).build()
        ));
        log.info("Seeded product catalogue with {} items", productRepository.count());
    }
}
