package com.kitcheninventory.inventory_project_backend.model;

import lombok.*;

import java.io.Serializable;

/*
 * Composite key implementation for JPA
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemId implements Serializable {
    private Long itemID;
    private Long purchaseID;
}
