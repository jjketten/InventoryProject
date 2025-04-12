package com.jamiekettenhofen.inventory_project_backend.dto;

import java.time.LocalDate;
import java.util.List;

public record PurchaseDTO(Long purchaseID, double totalCost, LocalDate date, String store, List<PurchaseItemDTO> items) {}