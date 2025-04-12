package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {
    @Id
    private Long itemID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "itemID")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "categoryID")
    private Category category;
}