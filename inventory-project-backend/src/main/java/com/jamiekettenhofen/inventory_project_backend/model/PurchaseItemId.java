package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/*
 * Composite key implementation for JPA
 */

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemId implements Serializable {
    private Long purchaseID;
    private Long itemID;
}
 