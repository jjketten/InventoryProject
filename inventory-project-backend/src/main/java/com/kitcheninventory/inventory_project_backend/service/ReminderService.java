package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;
import com.kitcheninventory.inventory_project_backend.repository.ReminderNativeRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        OffsetDateTime dateTime = null;

        
        if (dto.dateTime() != null) {
            // Convert from ISO date with Z to LocalDateTime
            // OffsetDateTime offsetDateTime = OffsetDateTime.parse(dto.dateTime().toString());
            // dateTime = offsetDateTime.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            dateTime = OffsetDateTime.parse((dto.dateTime()).toString());
            // dateTime = offsetDateTime.toLocalDateTime();

        } else {
            System.out.println("[addReminder] : dto.datetime is  null!!");
            dateTime = null;
        }

        System.out.println("[addReminder] : actual datetime is " + (dateTime == null ? "null" : dateTime.toString()));

        reminderRepository.insertReminder(
                dto.itemID(),
                dto.purchaseID(),
                dateTime,
                dto.completed(),
                dto.description()
        );
    }

    public void deleteReminder(Long itemID, Long purchaseID) {
        reminderRepository.deleteReminder(itemID, purchaseID);
    }

    public void markCompleted(Long itemID, Long purchaseID, boolean completed) {
        reminderRepository.updateCompletionStatus(itemID, purchaseID, completed);
    }
}
