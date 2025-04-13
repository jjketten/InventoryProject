package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseID;
    private double totalCost;
    private LocalDate date;
    private String store;

    @OneToMany(mappedBy = "purchase")
    private Set<PurchaseItem> purchaseItems;

    @OneToOne(mappedBy = "purchase")
    private Reminder reminder;
}