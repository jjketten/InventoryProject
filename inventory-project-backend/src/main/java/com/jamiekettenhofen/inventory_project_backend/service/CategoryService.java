package com.jamiekettenhofen.inventory_project_backend.service;

import com.jamiekettenhofen.inventory_project_backend.dto.CategoryDTO;
import com.jamiekettenhofen.inventory_project_backend.model.Category;
import com.jamiekettenhofen.inventory_project_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDTO(category.getCategoryID(), category.getName()))
                .toList();
    }

    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(category -> new CategoryDTO(category.getCategoryID(), category.getName()));
    }

    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.name());
        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getCategoryID(), saved.getName());
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
} 
