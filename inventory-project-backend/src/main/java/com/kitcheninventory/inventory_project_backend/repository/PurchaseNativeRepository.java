package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;

import java.util.List;

public interface PurchaseNativeRepository {

    PurchaseDTO createPurchase(PurchaseDTO dto);
    List<PurchaseDTO> getAllPurchases();
    PurchaseDTO getPurchaseById(Long id);
    void deletePurchase(Long id);
}
