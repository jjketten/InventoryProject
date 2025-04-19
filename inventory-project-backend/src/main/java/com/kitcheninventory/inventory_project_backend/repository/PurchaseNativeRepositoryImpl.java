package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.PurchaseDTO;
import com.kitcheninventory.inventory_project_backend.dto.PurchaseItemDTO;
import com.kitcheninventory.inventory_project_backend.model.Purchase;
import com.kitcheninventory.inventory_project_backend.model.PurchaseItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PurchaseNativeRepositoryImpl implements PurchaseNativeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public PurchaseDTO createPurchase(PurchaseDTO dto) {
        Connection conn = entityManager.unwrap(Connection.class);
        Long purchaseId;

        try {
            //insert the purchase
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO purchase (total_cost, date, store) VALUES (?, ?, ?) RETURNING purchase_id"
            );
            ps.setDouble(1, dto.totalCost());
            ps.setDate(2, Date.valueOf(dto.date()));
            ps.setString(3, dto.store());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                purchaseId = rs.getLong(1);
            } else {
                throw new SQLException("Failed to insert purchase");
            }

            //insert the items for that puchase
            for (PurchaseItemDTO item : dto.items()) {
                PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO purchase_item (purchase_id, item_id, unit, amount, price) VALUES (?, ?, ?, ?, ?)"
                );
                psItem.setLong(1, purchaseId);
                psItem.setLong(2, item.itemID());
                psItem.setString(3, item.unit());
                psItem.setInt(4, item.amount());
                psItem.setDouble(5, item.price());
                psItem.executeUpdate();
            }

            return new PurchaseDTO(purchaseId, dto.totalCost(), dto.date(), dto.store(), dto.items());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create purchase", e);
        }
    }

    @Override
    public List<PurchaseDTO> getAllPurchases() {
        Connection conn = entityManager.unwrap(Connection.class);
        List<PurchaseDTO> purchases = new ArrayList<>();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM purchase");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Long purchaseId = rs.getLong("purchase_id");

                List<PurchaseItemDTO> items = getItemsForPurchase(purchaseId);

                purchases.add(new PurchaseDTO(
                        purchaseId,
                        rs.getDouble("total_cost"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("store"),
                        items
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve purchases", e);
        }

        return purchases;
    }

    @Override
    public PurchaseDTO getPurchaseById(Long id) {
        Connection conn = entityManager.unwrap(Connection.class);

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM purchase WHERE purchase_id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                List<PurchaseItemDTO> items = getItemsForPurchase(id);

                return new PurchaseDTO(
                        id,
                        rs.getDouble("total_cost"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("store"),
                        items
                );
            } else {
                throw new RuntimeException("Purchase not found: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve purchase", e);
        }
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Connection conn = entityManager.unwrap(Connection.class);

        try {
            PreparedStatement deleteItems = conn.prepareStatement(
                "DELETE FROM purchase_item WHERE purchase_id = ?"
            );
            deleteItems.setLong(1, id);
            deleteItems.executeUpdate();

            PreparedStatement deletePurchase = conn.prepareStatement(
                "DELETE FROM purchase WHERE purchase_id = ?"
            );
            deletePurchase.setLong(1, id);
            deletePurchase.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete purchase: " + id, e);
        }
    }

    private List<PurchaseItemDTO> getItemsForPurchase(Long purchaseId) throws SQLException {
        Connection conn = entityManager.unwrap(Connection.class);
        List<PurchaseItemDTO> items = new ArrayList<>();

        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM purchase_item WHERE purchase_id = ?"
        );
        ps.setLong(1, purchaseId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            items.add(new PurchaseItemDTO(
                    rs.getLong("item_id"),
                    rs.getString("unit"),
                    rs.getInt("amount"),
                    rs.getDouble("price")
            ));
        }

        return items;
    }
}
