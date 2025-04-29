package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.ItemDTO;
import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;
import com.kitcheninventory.inventory_project_backend.dto.PurchaseItemDTO;
import com.kitcheninventory.inventory_project_backend.repository.ItemNativeRepository;
import com.kitcheninventory.inventory_project_backend.repository.PurchaseNativeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseNativeRepository purchaseRepository;
    private final ItemNativeRepository     itemRepo;

    // public PurchaseService(PurchaseNativeRepository purchaseRepository) {
    //     this.purchaseRepository = purchaseRepository;
    //     this.itemRepo = itemRepo;
    // }

    public PurchaseDTO createPurchase(PurchaseDTO dto) {
        return purchaseRepository.createPurchase(dto);
    }

    public List<PurchaseDTO> getAllPurchases() {
        return purchaseRepository.getAllPurchases();
    }

    public PurchaseDTO getPurchaseById(Long id) {
        return purchaseRepository.getPurchaseById(id);
    }

    public void deletePurchase(Long id) {
        purchaseRepository.deletePurchase(id);
    }

    @Transactional
    public PurchaseDTO createPurchaseAndAdjustInventory(PurchaseDTO dto) {
        List<PurchaseItemDTO> adjustedItems = new ArrayList<>();
    
        for (PurchaseItemDTO incoming : dto.items()) {
            boolean handled = false;
    
            //1) try find by ID
            if (incoming.itemID() != null && incoming.itemID() > 0) {
                Optional<ItemDTO> foundById = itemRepo.findItemById(incoming.itemID());
                if (foundById.isPresent()) {
                    ItemDTO existing = foundById.get();
    
                    if (!existing.name().equals(incoming.name())) {
                        throw new IllegalArgumentException(
                            "Item name mismatch for ID " + incoming.itemID() +
                            ": expected='" + existing.name() + "' got='" + incoming.name() + "'"
                        );
                    }
                    if (!existing.unit().equals(incoming.unit())) {
                        throw new IllegalArgumentException(
                            "Unit mismatch for ID " + incoming.itemID() +
                            ": expected='" + existing.unit() + "' got='" + incoming.unit() + "'"
                        );
                    }

                    System.out.println("[createPurchaseAndAdjustInventory] Matched by ID: " + incoming.itemID());
    
                    //1c) successful ID match: bump amount and add categories
                    float newAmount = existing.amount() + incoming.amount();
                    itemRepo.updateItem(existing.itemID(), existing.name(), existing.brand(), existing.unit(), newAmount);
    
                    if (!incoming.categories().isEmpty()) {
                        itemRepo.addItemCategoriesByName(existing.itemID(), incoming.categories());
                    }
    
                    adjustedItems.add(incoming);
                    handled = true;
                }
            }
    
            //2) if that didnt work, try find by name but only if there isnt a supplied id.
            if (incoming.itemID() == null || incoming.itemID() <= 0 && !handled) {
                List<ItemDTO> matchesByName = itemRepo.findItemsByName(incoming.name());
    
                for (ItemDTO candidate : matchesByName) {
                    if (candidate.brand().equals(incoming.brand()) && candidate.unit().equals(incoming.unit())) {
                        //perfect match: bump amount and add categories
                        System.out.println("[createPurchaseAndAdjustInventory] Matched by name: " + incoming.name());
                        float newAmount = candidate.amount() + incoming.amount();
                        itemRepo.updateItem(candidate.itemID(), candidate.name(), candidate.brand(), candidate.unit(), newAmount);
    
                        if (!incoming.categories().isEmpty()) {
                            itemRepo.addItemCategoriesByName(candidate.itemID(), incoming.categories());
                        }
    
                        adjustedItems.add(new PurchaseItemDTO(
                            candidate.itemID(),
                            candidate.name(),
                            candidate.brand(),
                            candidate.categories(),
                            candidate.unit(),
                            incoming.amount(),
                            incoming.price()
                        ));
                        handled = true;
                        break;
                    }
                }
            }
    
            //3) if still not handled,or if distinct valid id given, insert new item
            if (!handled) {
                System.out.println("[createPurchaseAndAdjustInventory] No match found. Creating new item: " + incoming.name() + ", received id: " + incoming.itemID());
                ItemDTO toCreate = new ItemDTO(
                    //We want to use the db to autoincrement if no itemid is supplied, but sometimes we want to submit with our own id.
                    ((incoming.itemID() != null && (incoming.itemID() > 0 )) ? (incoming.itemID()) : null),
                    incoming.name(),
                    incoming.brand(),
                    incoming.unit(),
                    incoming.amount(),
                    incoming.categories()
                );
    
                ItemDTO created = itemRepo.saveItemWithCategories(toCreate);
    
                adjustedItems.add(new PurchaseItemDTO(
                    created.itemID(),
                    created.name(),
                    created.brand(),
                    created.categories(),
                    created.unit(),
                    incoming.amount(),
                    incoming.price()
                ));
            }
        }
    
        //finally insert purchase
        PurchaseDTO toInsert = new PurchaseDTO(
            Long.valueOf(-1),
            dto.totalCost(),
            dto.date(),
            dto.store(),
            adjustedItems
        );
    
        return purchaseRepository.createPurchase(toInsert);
    }
    
    
}
