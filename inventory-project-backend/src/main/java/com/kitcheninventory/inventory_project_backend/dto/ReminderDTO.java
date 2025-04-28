package com.kitcheninventory.inventory_project_backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetDateTime;

import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;


import jakarta.persistence.Entity;
public record ReminderDTO(
    Long itemID,
    String itemName,
    String itemBrand,
    String itemUnit,
    float itemAmount,

    Long purchaseID,
    LocalDate purchaseDate,
    String purchaseStore,
    double purchaseTotalCost,

    // OffsetDateTime dateTime,
    OffsetDateTime dateTime,
    boolean completed,

    String description
) {}
