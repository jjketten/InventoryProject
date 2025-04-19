package com.kitcheninventory.inventory_project_backend.model;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeItemId implements Serializable {
    private Long recipeID;
    private Long itemID;
}
