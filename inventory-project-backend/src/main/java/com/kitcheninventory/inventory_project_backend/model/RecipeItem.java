package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recipe_item")
@IdClass(RecipeItemId.class)
public class RecipeItem {

    @Id
    private Long recipeID;

    @Id
    private Long itemID;

    private String unit;
    private int amount;

    @ManyToOne
    @JoinColumn(name = "recipeID", insertable = false, updatable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "itemID", insertable = false, updatable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
