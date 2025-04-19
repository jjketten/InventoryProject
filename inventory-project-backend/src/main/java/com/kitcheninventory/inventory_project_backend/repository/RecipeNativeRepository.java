package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.RecipeDTO;
import java.util.List;
import java.util.Optional;

public interface RecipeNativeRepository {
    Long createRecipe(RecipeDTO dto);
    List<RecipeDTO> getAllRecipes();
    Optional<RecipeDTO> getRecipeById(Long id);
    void deleteRecipe(Long id);
    RecipeDTO updateRecipe(RecipeDTO dto);

}
