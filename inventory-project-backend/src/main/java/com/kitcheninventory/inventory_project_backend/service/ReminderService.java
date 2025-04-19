package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;
import com.kitcheninventory.inventory_project_backend.repository.ReminderNativeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final ReminderNativeRepository reminderRepository;

    public ReminderService(ReminderNativeRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public List<ReminderDTO> getAllReminders() {
        return reminderRepository.findAllReminders();
    }

    public void addReminder(ReminderDTO dto) {
        reminderRepository.insertReminder(
                dto.itemID(),
                dto.purchaseID(),
                dto.dateTime(),
                dto.completed()
        );
    }

    public void deleteReminder(Long itemID, Long purchaseID) {
        reminderRepository.deleteReminder(itemID, purchaseID);
    }

    public void markCompleted(Long itemID, Long purchaseID, boolean completed) {
        reminderRepository.updateCompletionStatus(itemID, purchaseID, completed);
    }
}
