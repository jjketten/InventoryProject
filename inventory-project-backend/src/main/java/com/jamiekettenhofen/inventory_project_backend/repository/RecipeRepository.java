package com.jamiekettenhofen.inventory_project_backend.repository;

import com.jamiekettenhofen.inventory_project_backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {}
