package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;
import com.kitcheninventory.inventory_project_backend.dto.PurchaseItemDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PurchaseNativeRepositoryImpl implements PurchaseNativeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public PurchaseDTO createPurchase(PurchaseDTO dto) {
        // 1) Insert the purchase record
        Number purchaseIdNum = (Number) entityManager.createNativeQuery("""
            INSERT INTO purchase (totalcost, date, store)
            VALUES (:totalCost, :date, :store)
            RETURNING purchase_id
        """)
        .setParameter("totalCost", dto.totalCost())
        .setParameter("date", Date.valueOf(dto.date()))
        .setParameter("store", dto.store())
        // .executeUpdate();
        .getSingleResult();

        Long purchaseId = purchaseIdNum.longValue();

        // 2) Pull back the generated purchase_id
        // Long purchaseId = ((Number) entityManager
        //     .createNativeQuery("SELECT currval(pg_get_serial_sequence('purchase','purchase_id'))")
        //     .getSingleResult())
        //     .longValue();

        // 3) Insert each purchase_item row
        for (PurchaseItemDTO item : dto.items()) {
            entityManager.createNativeQuery("""
                INSERT INTO purchase_item (purchase_id, item_id, unit, amount, price)
                VALUES (:purchaseId, :itemId, :unit, :amount, :price)
            """)
            .setParameter("purchaseId", purchaseId)
            .setParameter("itemId",    item.itemID())
            .setParameter("unit",      item.unit())
            .setParameter("amount",    item.amount())
            .setParameter("price",     item.price())
            .executeUpdate();
        }

        // 4) Return a DTO with the new ID and original payload
        return new PurchaseDTO(
            purchaseId,
            dto.totalCost(),
            dto.date(),
            dto.store(),
            dto.items()
        );
    }

    @Override
    public List<PurchaseDTO> getAllPurchases() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT purchase_id, totalcost, date, store
            FROM purchase
            ORDER BY purchase_id
        """).getResultList();

        List<PurchaseDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long       id        = ((Number) row[0]).longValue();
            double     cost      = ((Number) row[1]).doubleValue();
            LocalDate  date      = ((Date)   row[2]).toLocalDate();
            String     store     = (String)  row[3];
            List<PurchaseItemDTO> items = getItemsForPurchase(id);

            result.add(new PurchaseDTO(id, cost, date, store, items));
        }
        return result;
    }

    @Override
    public PurchaseDTO getPurchaseById(Long id) {
        Object[] row = (Object[]) entityManager.createNativeQuery("""
            SELECT purchase_id, totalcost, date, store
            FROM purchase
            WHERE purchase_id = :id
        """)
        .setParameter("id", id)
        .getSingleResult();

        Long       purchaseId = ((Number) row[0]).longValue();
        double     cost       = ((Number) row[1]).doubleValue();
        LocalDate  date       = ((Date)   row[2]).toLocalDate();
        String     store      = (String)  row[3];
        List<PurchaseItemDTO> items = getItemsForPurchase(purchaseId);

        return new PurchaseDTO(purchaseId, cost, date, store, items);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        entityManager.createNativeQuery("""
            DELETE FROM purchase_item
            WHERE purchase_id = :id
        """)
        .setParameter("id", id)
        .executeUpdate();

        entityManager.createNativeQuery("""
            DELETE FROM purchase
            WHERE purchase_id = :id
        """)
        .setParameter("id", id)
        .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private List<String> getCategoriesForItem(Long itemId) { //prob should use item or category repository for this
        return entityManager.createNativeQuery("""
            SELECT c.name
            FROM category c
            JOIN item_category ic ON c.category_id = ic.category_id
            WHERE ic.item_id = :itemId
            """)
        .setParameter("itemId", itemId)
        .getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<PurchaseItemDTO> getItemsForPurchase(Long purchaseId) {
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT item.item_id, item.name, item.brand, purchase_item.unit, purchase_item.amount, purchase_item.price
            FROM (purchase_item LEFT JOIN item ON purchase_item.item_id = item.item_id)
            WHERE purchase_id = :id
        """)
        .setParameter("id", purchaseId)
        .getResultList();

        List<PurchaseItemDTO> items = new ArrayList<>();
        for (Object[] row : rows) {
            Long   itemId   = ((Number) row[0]).longValue();
            String name     = (String) row[1];
            String brand    = (String) row[2];
            String unit     = (String) row[3];
            int    amount   = ((Number) row[4]).intValue();
            double price    = ((Number) row[5]).doubleValue();

            // fetch the categories
            List<String> categories = getCategoriesForItem(itemId);

            items.add(new PurchaseItemDTO(
                itemId,
                name,
                brand,
                categories,
                unit,
                amount,
                price
            ));
        }
        return items;
    }
}
