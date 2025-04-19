package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import com.kitcheninventory.inventory_project_backend.dto.ItemCategoryDTO;
import com.kitcheninventory.inventory_project_backend.model.Category;
import com.kitcheninventory.inventory_project_backend.model.Item;
import com.kitcheninventory.inventory_project_backend.model.ItemCategory;
import com.kitcheninventory.inventory_project_backend.repository.CategoryRepository;
import com.kitcheninventory.inventory_project_backend.repository.ItemCategoryRepository;
import com.kitcheninventory.inventory_project_backend.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemCategoryService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    public ItemCategoryService(ItemRepository itemRepository,
                                CategoryRepository categoryRepository,
                                ItemCategoryRepository itemCategoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }

    public Optional<ItemCategoryDTO> assignCategoryToItem(Long itemId, Long categoryId) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (itemOpt.isPresent() && categoryOpt.isPresent()) {
            Item item = itemOpt.get();
            Category category = categoryOpt.get();

            ItemCategory itemCategory = new ItemCategory();
            itemCategory.setItem(item);
            itemCategory.setCategory(category);
            itemCategory.setItemID(itemId);

            itemCategoryRepository.save(itemCategory);

            CategoryDTO categoryDTO = new CategoryDTO(category.getCategoryID(), category.getName());
            return Optional.of(new ItemCategoryDTO(item.getItemID(), item.getName(), categoryDTO));
        }

        return Optional.empty();
    }

    public void removeCategoryFromItem(Long itemId) {
        itemCategoryRepository.deleteById(itemId);
    }
} 
