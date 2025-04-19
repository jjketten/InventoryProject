package com.kitcheninventory.inventory_project_backend.service;


import com.kitcheninventory.inventory_project_backend.dto.ItemDTO;
import com.kitcheninventory.inventory_project_backend.model.Item;
import com.kitcheninventory.inventory_project_backend.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(item -> new ItemDTO(
                        item.getItemID(),
                        item.getName(),
                        item.getBrand(),
                        item.getUnit(),
                        item.getAmount()
                ))
                .toList();
    }

    public Optional<ItemDTO> getItemById(Long id) {
        return itemRepository.findById(id)
                .map(item -> new ItemDTO(
                        item.getItemID(),
                        item.getName(),
                        item.getBrand(),
                        item.getUnit(),
                        item.getAmount()
                ));
    }

    public ItemDTO createItem(ItemDTO dto) {
        Item item = new Item();
        item.setName(dto.name());
        item.setBrand(dto.brand());
        item.setUnit(dto.unit());
        item.setAmount(dto.amount());

        Item saved = itemRepository.save(item);
        return new ItemDTO(saved.getItemID(), saved.getName(), saved.getBrand(), saved.getUnit(), saved.getAmount());
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
} 
