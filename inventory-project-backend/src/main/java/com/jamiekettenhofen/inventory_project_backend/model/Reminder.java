package com.jamiekettenhofen.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReminderId.class)
public class Reminder {
    private Long itemID;
    private Long purchaseID;
}
