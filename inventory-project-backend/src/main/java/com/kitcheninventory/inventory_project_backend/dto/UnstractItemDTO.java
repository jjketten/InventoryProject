package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnstractItemDTO {
    private String brand;
    private String cost;
    private String itemName;
    private String productCode;
    private String quantity;
    private String units;
    private List<String> categories;
}
