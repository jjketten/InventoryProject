package com.kitcheninventory.inventory_project_backend.model;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderId implements Serializable {
    private Long itemID;
    private Long purchaseID;
    private String description;
}
