package com.kitcheninventory.inventory_project_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;


import jakarta.persistence.Entity;

public record ReminderDTO(
    Long itemID,
    String itemName,
    String itemBrand,
    String itemUnit,
    int itemAmount,

    Long purchaseID,
    LocalDate purchaseDate,
    String purchaseStore,
    double purchaseTotalCost,

    LocalDateTime dateTime,
    boolean completed
) {}
