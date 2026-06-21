package com.javatechdebasis.ecommerce.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    /** Stock physically on hand. */
    @Column(nullable = false)
    private int availableQuantity;

    /** Stock reserved for in-flight orders (not yet shipped). */
    @Column(nullable = false)
    private int reservedQuantity;

    /** Optimistic lock guards against concurrent reservations of the same product. */
    @Version
    private Long version;
}
