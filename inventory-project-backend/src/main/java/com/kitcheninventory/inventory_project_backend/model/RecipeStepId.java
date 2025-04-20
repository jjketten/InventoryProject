package com.kitcheninventory.inventory_project_backend.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepId implements Serializable {
    private Long recipe;
    private int stepNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeStepId that)) return false;
        return stepNumber == that.stepNumber &&
               Objects.equals(recipe, that.recipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, stepNumber);
    }
}
