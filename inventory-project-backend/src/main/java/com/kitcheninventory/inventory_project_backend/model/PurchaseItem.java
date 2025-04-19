package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_item") //Has(PurchaseID,ItemID,Unit,Amount,Price)
@IdClass(PurchaseItemId.class)
public class PurchaseItem {

    @Id
    @Column(name = "itemID")
    private Long itemID;

    @Id
    @Column(name = "purchaseID")
    private Long purchaseID;

    private String unit;

    private int amount;

    private double price;

    @ManyToOne
    @JoinColumn(name = "itemID", insertable = false, updatable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "purchaseID", insertable = false, updatable = false)
    private Purchase purchase;
}
