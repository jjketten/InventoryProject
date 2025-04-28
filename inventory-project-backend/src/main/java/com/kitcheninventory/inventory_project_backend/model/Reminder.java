package com.kitcheninventory.inventory_project_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;

@SqlResultSetMapping(
    name = "ReminderDTOMapping",
    classes = @ConstructorResult(
        targetClass = ReminderDTO.class,
        columns = {
            @ColumnResult(name = "item_id", type = Long.class),
            @ColumnResult(name = "item_name", type = String.class),
            @ColumnResult(name = "brand", type = String.class),
            @ColumnResult(name = "unit", type = String.class),
            @ColumnResult(name = "amount", type = Integer.class),
            @ColumnResult(name = "purchase_id", type = Long.class),
            @ColumnResult(name = "date", type = java.time.LocalDate.class),
            @ColumnResult(name = "store", type = String.class),
            @ColumnResult(name = "totalcost", type = Double.class),
            @ColumnResult(name = "datetime", type = java.time.OffsetDateTime.class),
            @ColumnResult(name = "completed", type = Boolean.class),
            @ColumnResult(name = "description", type = String.class)
        }
    )
)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReminderId.class)
@Table(name = "reminder")
public class Reminder {

    @Id
    @Column(name = "item_id")
    @JoinColumn(name = "item_id")
    private Long itemID;

    @Id
    @JoinColumn(name = "purchase_id")
    @Column(name = "purchase_id")
    
    private Long purchaseID;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "datetime")
    private OffsetDateTime dateTime;

    @Id
    @Column(name = "description")
    private String description;
}
