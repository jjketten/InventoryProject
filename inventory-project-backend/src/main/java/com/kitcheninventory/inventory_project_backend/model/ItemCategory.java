// model/ItemCategory.java
package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ItemCategoryId.class)
@Table(name = "item_category")
public class ItemCategory {

    @Id
    @Column(name = "item_ID")
    private Long itemID;

    @Id
    @Column(name = "category_ID")
    private Long categoryID;

    @ManyToOne
    @JoinColumn(name = "item_ID", insertable = false, updatable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "category_ID", insertable = false, updatable = false)
    private Category category;
}
