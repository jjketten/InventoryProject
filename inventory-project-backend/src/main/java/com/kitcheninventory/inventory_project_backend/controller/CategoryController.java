package com.kitcheninventory.inventory_project_backend.controller;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import com.kitcheninventory.inventory_project_backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:8081")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Get all categories
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    // Create a new category
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO created = categoryService.createCategory(categoryDTO.name());
        return ResponseEntity.ok(created);
    }

    // Rename an existing category
    @PutMapping("/{id}/rename")
    public ResponseEntity<Void> renameCategory(@PathVariable Long id, @RequestBody String newName) {
        categoryService.renameCategory(id, newName);
        return ResponseEntity.ok().build();
    }

    // Delete a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Add item to category
    @PostMapping("/{categoryId}/items/{itemId}")
    public ResponseEntity<Void> addItemToCategory(@PathVariable Long categoryId, @PathVariable Long itemId) {
        categoryService.addItemToCategory(categoryId, itemId);
        return ResponseEntity.ok().build();
    }
}
