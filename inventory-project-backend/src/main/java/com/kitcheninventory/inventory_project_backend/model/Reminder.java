package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReminderId.class)
@Table(name = "reminder")
public class Reminder {

    @Id
    @Column(name = "item_id")
    private Long itemID;

    @Id
    @Column(name = "purchase_id")
    private Long purchaseID;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "date_time")
    private LocalDateTime dateTime;
}
