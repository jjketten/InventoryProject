package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Set; 

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recipeID;
    private String name;
    private String reference;

    @OneToMany(mappedBy = "recipe")
    private Set<RecipeItem> recipeItems;

    @OneToMany(mappedBy = "recipe")
    private List<RecipeStep> steps;
}
