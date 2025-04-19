package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.ItemDTO;
import com.kitcheninventory.inventory_project_backend.model.Item;
import com.kitcheninventory.inventory_project_backend.repository.ItemNativeRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemNativeRepository itemNativeRepository;

    public ItemDTO createItem(ItemDTO dto) {
        return itemNativeRepository.saveItemWithCategories(dto);
    }

    public List<ItemDTO> getAllItems() {
        return itemNativeRepository.getAllItemsWithCategories();
    }

    public Optional<ItemDTO> getItemById(Long id) {
        return itemNativeRepository.getItemWithCategoriesById(id);
    }

    public void deleteItem(Long id) {
        itemNativeRepository.deleteItemAndAssociations(id);
    }

    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO dto) {
        Optional<ItemDTO> existing = itemNativeRepository.findItemById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Item not found with ID: " + id);
        }

        itemNativeRepository.updateItem(id, dto.name(), dto.brand(), dto.unit(), dto.amount());

        if (dto.categories() != null) {
            itemNativeRepository.clearItemCategories(id); // deletes from item_category
            itemNativeRepository.addItemCategoriesByName(id, dto.categories());
        }

        return getItemById(id).orElseThrow(); // get updated version
    }

}
