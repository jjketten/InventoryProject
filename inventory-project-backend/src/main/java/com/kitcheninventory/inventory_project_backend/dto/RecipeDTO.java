package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

public record RecipeDTO(
    Long recipeID,
    String name,
    String reference,
    List<RecipeItemDTO> items,
    List<RecipeStepDTO> steps
) {}
