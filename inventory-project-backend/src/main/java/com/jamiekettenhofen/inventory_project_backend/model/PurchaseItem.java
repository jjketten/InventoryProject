package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

/*
 * "Has" relationship
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PurchaseItemId.class)
public class PurchaseItem {
    @Id
    private Long purchaseID;
    @Id
    private Long itemID;

    @ManyToOne
    @MapsId("purchaseID")
    @JoinColumn(name = "purchaseID")
    private Purchase purchase;

    @ManyToOne
    @MapsId("itemID")
    @JoinColumn(name = "itemID")
    private Item item;

    private String unit;
    private int amount;
    private double price;
}
