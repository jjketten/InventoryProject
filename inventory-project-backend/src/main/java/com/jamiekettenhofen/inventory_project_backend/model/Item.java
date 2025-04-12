package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemID;
    private String name;
    private String brand;
    private String unit;
    private int amount;

    @OneToMany(mappedBy = "item")
    private Set<PurchaseItem> purchaseItems;

    @OneToMany(mappedBy = "item")
    private Set<RecipeItem> recipeItems;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL)
    private ItemCategory category;

    @OneToOne(mappedBy = "reminderID.item")
    private Reminder reminder;
}
