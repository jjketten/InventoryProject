package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_category")
public class ItemCategory {
    @Id
    @Column(name = "item_ID")
    private Long itemID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_ID")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "category_ID")
    private Category category;
}