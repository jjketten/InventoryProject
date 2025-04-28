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
    @Column(name = "item_ID")
    private Long itemID;

    @Id
    @Column(name = "purchase_ID")
    private Long purchaseID;

    private String unit;

    private float amount;

    private double price;

    @ManyToOne
    @JoinColumn(name = "item_ID", insertable = false, updatable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "purchase_ID", insertable = false, updatable = false)
    private Purchase purchase;
}
