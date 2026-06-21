package com.javatechdebasis.ecommerce.inventory.controller;

import com.javatechdebasis.ecommerce.inventory.entity.Product;
import com.javatechdebasis.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /** Read-only view of current stock; handy for demos and verifying the saga. */
    @GetMapping
    public List<Product> getAll() {
        return inventoryService.findAll();
    }
}
