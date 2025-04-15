package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryID;

    private String name;

    @OneToMany(mappedBy = "category")
    private Set<ItemCategory> itemCategories;
}
