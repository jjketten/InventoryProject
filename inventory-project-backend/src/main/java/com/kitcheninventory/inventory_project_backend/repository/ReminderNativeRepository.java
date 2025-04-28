package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReminderNativeRepository {
    List<ReminderDTO> findAllReminders();
    void insertReminder(Long itemID, Long purchaseID, OffsetDateTime dateTime, boolean completed, String desc);
    void deleteReminder(Long itemID, Long purchaseID);
    void updateCompletionStatus(Long itemID, Long purchaseID, boolean completed);
}
