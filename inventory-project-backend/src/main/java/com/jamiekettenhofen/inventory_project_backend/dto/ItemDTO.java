package com.jamiekettenhofen.inventory_project_backend.dto;

public record ItemDTO(Long itemID, String name, String brand, String unit, int amount) {}