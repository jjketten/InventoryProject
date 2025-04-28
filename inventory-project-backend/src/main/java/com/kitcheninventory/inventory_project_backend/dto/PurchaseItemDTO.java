package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

public record PurchaseItemDTO(
    Long itemID,
    String name,
    String brand,
    List<String> categories,
    String unit,
    float amount,
    double price
) {}
