package com.kitcheninventory.inventory_project_backend.dto;

public record RecipeItemDTO(
    Long itemID,
    String unit,
    int amount,
    Long categoryID,
    String categoryName
) {}
