package com.kitcheninventory.inventory_project_backend.dto;

import java.time.LocalDateTime;

public record ReminderDTO(Long itemID, Long purchaseID, String description, LocalDateTime dateTime, boolean completed) {}
