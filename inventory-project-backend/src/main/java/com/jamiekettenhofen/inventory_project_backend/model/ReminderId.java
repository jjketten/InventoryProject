package com.jamiekettenhofen.inventory_project_backend.model;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

//Non-entity
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderId implements Serializable {
    private Long itemID;
    private Long purchaseID;
}

