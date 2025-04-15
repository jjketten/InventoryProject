package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "purchase_ID")
    private Purchase purchase;

    @OneToOne
    @JoinColumn(name = "item_ID")
    private Item item;

    private boolean Completed;
    private LocalDateTime dateTime;
}
