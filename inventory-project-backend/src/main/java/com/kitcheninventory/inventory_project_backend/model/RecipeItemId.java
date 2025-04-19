package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeItemId implements Serializable {
    private Long recipeID;
    private Long itemID;
}
