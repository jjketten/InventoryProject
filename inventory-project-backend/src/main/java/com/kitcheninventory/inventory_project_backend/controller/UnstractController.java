package com.kitcheninventory.inventory_project_backend.controller;

import com.kitcheninventory.inventory_project_backend.dto.UnstractResultDTO;
import com.kitcheninventory.inventory_project_backend.service.UnstractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/unstract")
@RequiredArgsConstructor
public class UnstractController {

    private final UnstractService unstractService;

    @PostMapping("/upload")
    public ResponseEntity<UnstractResultDTO> uploadReceipt(@RequestParam("file") MultipartFile file) {
        try {
            UnstractResultDTO result = unstractService.uploadAndParse(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
