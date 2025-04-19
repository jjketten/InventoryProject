package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.RecipeDTO;
import com.kitcheninventory.inventory_project_backend.repository.RecipeNativeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeNativeRepository recipeNativeRepository;

    public RecipeService(RecipeNativeRepository recipeNativeRepository) {
        this.recipeNativeRepository = recipeNativeRepository;
    }

    // Create a new recipe (with items + steps)
    public Long createRecipe(RecipeDTO dto) {
        return recipeNativeRepository.createRecipe(dto);
    }

    // Get a recipe by ID
    public Optional<RecipeDTO> getRecipeById(Long id) {
        return recipeNativeRepository.getRecipeById(id);
    }

    // Get all recipes
    public List<RecipeDTO> getAllRecipes() {
        return recipeNativeRepository.getAllRecipes();
    }

    // Update recipe (and replace items + steps)
    public RecipeDTO updateRecipe(RecipeDTO dto) {
        return recipeNativeRepository.updateRecipe(dto);
    }

    // Delete a recipe and its associations
    public void deleteRecipe(Long id) {
        recipeNativeRepository.deleteRecipe(id);
    }
}
