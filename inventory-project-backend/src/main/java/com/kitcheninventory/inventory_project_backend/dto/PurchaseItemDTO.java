package com.kitcheninventory.inventory_project_backend.dto;

public record PurchaseItemDTO(
    Long itemID,
    String unit,
    int amount,
    double price
) {}
