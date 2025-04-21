package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import com.kitcheninventory.inventory_project_backend.dto.CategoryTotalDTO;
import com.kitcheninventory.inventory_project_backend.dto.CategoryUnitTotalDTO;

import java.util.List;

public interface CategoryNativeRepository {
    List<CategoryDTO> findAllCategories();
    CategoryDTO findCategoryById(Long categoryId);
    CategoryDTO createCategory(String name);
    void renameCategory(Long categoryId, String newName);
    void deleteCategory(Long categoryId);
    void addItemToCategory(Long categoryId, Long itemId);
    List<Long> getItemIdsForCategory(Long categoryId);
    List<CategoryUnitTotalDTO> getCategoryTotalsByUnit(Long categoryId);
    List<CategoryTotalDTO> getAllCategoryTotals();
}
