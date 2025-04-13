package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReminderId.class)
public class Reminder {
    @Id
    private Long itemID;
    @Id
    private Long purchaseID;

    @OneToOne
    @JoinColumn(name = "purchaseID")
    private Purchase purchase;

    @OneToOne
    @JoinColumn(name = "itemID")
    private Item item;
}
