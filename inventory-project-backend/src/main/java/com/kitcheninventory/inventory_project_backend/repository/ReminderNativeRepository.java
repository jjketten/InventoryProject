package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderNativeRepository {
    List<ReminderDTO> findAllReminders();
    void insertReminder(Long itemID, Long purchaseID, LocalDateTime dateTime, boolean completed);
    void deleteReminder(Long itemID, Long purchaseID);
    void updateCompletionStatus(Long itemID, Long purchaseID, boolean completed);
}
