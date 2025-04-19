package com.kitcheninventory.inventory_project_backend.service;

import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;
import com.kitcheninventory.inventory_project_backend.repository.PurchaseNativeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseNativeRepository purchaseRepository;

    public PurchaseService(PurchaseNativeRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

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
}
