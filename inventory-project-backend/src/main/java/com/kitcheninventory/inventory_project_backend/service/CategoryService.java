package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import com.kitcheninventory.inventory_project_backend.repository.CategoryNativeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryNativeRepository categoryRepository;

    public CategoryService(CategoryNativeRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllCategories();
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findCategoryById(id);
    }

    public CategoryDTO createCategory(String name) {
        return categoryRepository.createCategory(name);
    }

    public void renameCategory(Long id, String newName) {
        categoryRepository.renameCategory(id, newName);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteCategory(id);
    }

    public void addItemToCategory(Long categoryId, Long itemId) {
        categoryRepository.addItemToCategory(categoryId, itemId);
    }
}
