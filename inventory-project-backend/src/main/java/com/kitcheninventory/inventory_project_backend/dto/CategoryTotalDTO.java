package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

public record CategoryTotalDTO(
    Long categoryID,
    List<CategoryUnitTotalDTO> totals
) {}

