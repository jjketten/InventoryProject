package com.kitcheninventory.inventory_project_backend.controller;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;
import com.kitcheninventory.inventory_project_backend.service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "http://localhost:8081")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ResponseEntity<List<ReminderDTO>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    @PostMapping
    public ResponseEntity<String> addReminder(@RequestBody ReminderDTO dto) {
        reminderService.addReminder(dto);
        return ResponseEntity.ok("Reminder added successfully");
    }

    @DeleteMapping("/{itemId}/{purchaseId}")
    public ResponseEntity<String> deleteReminder(@PathVariable Long itemId, @PathVariable Long purchaseId) {
        reminderService.deleteReminder(itemId, purchaseId);
        return ResponseEntity.ok("Reminder deleted successfully");
    }

    // PATCH to update completion status
    @PatchMapping("/{itemId}/{purchaseId}/completion")
    public ResponseEntity<String> updateCompletionStatus(
            @PathVariable Long itemId,
            @PathVariable Long purchaseId,
            @RequestParam boolean completed
    ) {
        reminderService.markCompleted(itemId, purchaseId, completed);
        return ResponseEntity.ok("Completion status updated");
    }
}
