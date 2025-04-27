package com.kitcheninventory.inventory_project_backend.controller;

import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;
import com.kitcheninventory.inventory_project_backend.service.PurchaseService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "http://localhost:8081")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // Create a purchase
    // @PostMapping
    // public ResponseEntity<PurchaseDTO> createPurchase(@RequestBody PurchaseDTO dto) {
    //     PurchaseDTO created = purchaseService.createPurchase(dto);
    //     return ResponseEntity.ok(created);
    // }
    @PostMapping
    public ResponseEntity<?> createPurchase(@RequestBody PurchaseDTO dto) {
        try {
            PurchaseDTO created = purchaseService.createPurchaseAndAdjustInventory(dto);
            return ResponseEntity.ok(created);

        } catch (IllegalArgumentException iae) {
            // validation mismatch (name/unit)
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", iae.getMessage()));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                .status(500)
                .body(Map.of("error", "Unexpected server error"));
        }
    }

    // Get all purchases
    @GetMapping
    public ResponseEntity<List<PurchaseDTO>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    // Get purchase by ID
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseDTO> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    // Delete purchase
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }
}
