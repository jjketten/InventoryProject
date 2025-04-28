package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ItemDTO;
import com.kitcheninventory.inventory_project_backend.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemNativeRepository {
    ItemDTO saveItemWithCategories(ItemDTO dto);
    ItemDTO saveItemWithCategoriesWithID(ItemDTO dto);

    List<ItemDTO> getAllItemsWithCategories();

    Optional<ItemDTO> getItemWithCategoriesById(Long id);

    void deleteItemAndAssociations(Long id);

    void updateItem(Long id, String name, String brand, String unit, float amount);

    void clearItemCategories(Long itemId);

    void addItemCategoriesByName(Long itemId, List<String> categoryNames);

    Optional<ItemDTO> findItemById(Long itemId);

}
