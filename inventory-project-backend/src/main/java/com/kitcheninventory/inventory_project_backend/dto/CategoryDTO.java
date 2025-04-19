package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

public record CategoryDTO(
    Long categoryID,
    String name,
    List<Long> itemIDs 
) {}
