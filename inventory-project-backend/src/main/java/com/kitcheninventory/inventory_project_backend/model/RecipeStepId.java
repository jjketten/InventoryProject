package com.kitcheninventory.inventory_project_backend.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepId implements Serializable {
    private Long recipeID;
    private int stepNumber;
}

