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

     /**
     * 1) For each incoming PurchaseItemDTO:
     *    • if itemRepo.findItemById(id) is present:
     *         – compare name, brand, unit — if any mismatch, throw IllegalArgumentException
     *         – otherwise update item's amount = existing + new
     *    • else (does not exist) -> save a brand‐new item via itemRepo.saveItemWithCategories()
     *      and overwrite purchaseItem.itemID with the newly generated id
     * 2)Delegate to purchaseRepo.createPurchase()
     */
    @Transactional
    public PurchaseDTO createPurchaseAndAdjustInventory(PurchaseDTO dto) {
        List<PurchaseItemDTO> adjustedItems = new ArrayList<>();

        for (PurchaseItemDTO incoming : dto.items()) {
            Optional<ItemDTO> found = itemRepo.findItemById(incoming.itemID());

            if (found.isPresent()) {
                ItemDTO existing = found.get();

                // 1a) verify name/unit
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

                // 1b) bump the amount
                System.out.println("[createPurchaseAndAdjustInventory] successful item match, attempting to increase item quantity for item:" + existing.itemID());
                float newAmount = existing.amount() + incoming.amount();
                itemRepo.updateItem(
                    existing.itemID(),
                    existing.name(),
                    existing.brand(),
                    existing.unit(),
                    newAmount
                );
                
                // 1c) add new categories 
                if (!incoming.categories().isEmpty()) {
                    itemRepo.addItemCategoriesByName(
                      existing.itemID(),
                      incoming.categories()
                    );
                }

                adjustedItems.add(incoming);

            } else {
                // 1c) does not exist: insert new item
                ItemDTO toCreate = new ItemDTO(
                    ((incoming.itemID() > 0 ) ? incoming.itemID() : null),
                    incoming.name(),
                    incoming.brand(),
                    incoming.unit(),
                    incoming.amount(),
                    incoming.categories()
                );
                ItemDTO created;
                if(toCreate.itemID() == null) {
                    created = itemRepo.saveItemWithCategories(toCreate);
                }
                else {
                    created = itemRepo.saveItemWithCategoriesWithID(toCreate);
                }

                // now reference the newly assigned itemID
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

        // 2) build a fresh DTO (no ID) and hand off to your native repo
        PurchaseDTO toInsert = new PurchaseDTO(
            Long.valueOf(-1), //probably bad, but this wont get used
            dto.totalCost(),
            dto.date(),
            dto.store(),
            adjustedItems
        );

        return purchaseRepository.createPurchase(toInsert);
    }
}
