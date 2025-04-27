package com.kitcheninventory.inventory_project_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnstractResultDTO {
    private String date;
    private String storeName;
    private String tax;
    private String totalCost;
    private List<UnstractItemDTO> items;
}
