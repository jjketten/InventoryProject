package com.kitcheninventory.inventory_project_backend.controller;


import com.kitcheninventory.inventory_project_backend.dto.ItemCategoryDTO;
import com.kitcheninventory.inventory_project_backend.service.ItemCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item-categories")
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @PostMapping("/assign")
    public ResponseEntity<ItemCategoryDTO> assignCategoryToItem(@RequestParam Long itemId, @RequestParam Long categoryId) {
        return itemCategoryService.assignCategoryToItem(itemId, categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Void> removeCategoryFromItem(@PathVariable Long itemId) {
        itemCategoryService.removeCategoryFromItem(itemId);
        return ResponseEntity.noContent().build();
    }
} 
