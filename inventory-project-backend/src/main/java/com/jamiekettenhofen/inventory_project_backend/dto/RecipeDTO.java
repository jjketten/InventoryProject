package com.jamiekettenhofen.inventory_project_backend.dto;

import java.util.List;

public record RecipeDTO(Long recipeID, String name, String reference, List<RecipeItemDTO> ingredients, List<RecipeStepDTO> steps) {}