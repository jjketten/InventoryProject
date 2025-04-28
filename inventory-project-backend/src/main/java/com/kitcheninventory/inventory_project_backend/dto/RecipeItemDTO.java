package com.kitcheninventory.inventory_project_backend.dto;

public record RecipeItemDTO(
    Long itemID,
    String unit,
    float amount,
    Long categoryID,
    String categoryName
) {}
