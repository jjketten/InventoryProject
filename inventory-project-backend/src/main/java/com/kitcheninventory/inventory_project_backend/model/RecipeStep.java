package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RecipeStepId.class)
@Table(name = "recipe_step")
public class RecipeStep {

    @Id
    @Column(name = "recipe_id")
    private Long recipeID;

    @Id
    private int stepNumber;

    private String content;

    @ManyToOne
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
}
