package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RecipeItemId.class)
public class RecipeItem {
    @Id
    private Long recipeID;
    @Id
    private Long itemID;

    @ManyToOne
    @MapsId("recipeID")
    @JoinColumn(name = "recipeID")
    private Recipe recipe;

    @ManyToOne
    @MapsId("itemID")
    @JoinColumn(name = "itemID")
    private Item item;

    private String unit;
    private int amount;
}