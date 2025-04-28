package com.kitcheninventory.inventory_project_backend.dto;

import java.util.List;

public record ItemDTO(
    Long itemID,
    String name,
    String brand,
    String unit,
    float amount,
    List<String> categories //backend handles resolution of name vs id
) {}
