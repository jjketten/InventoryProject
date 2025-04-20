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
    @Column(name = "recipe_id")
    private Long recipeID;

    @Id
    @Column(name = "item_id")
    private Long itemID;

    private String unit;
    private int amount;

    @ManyToOne
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

