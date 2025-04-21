package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import com.kitcheninventory.inventory_project_backend.dto.CategoryUnitTotalDTO;
import com.kitcheninventory.inventory_project_backend.dto.CategoryTotalDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class CategoryNativeRepositoryImpl implements CategoryNativeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<CategoryDTO> findAllCategories() {
        var results = em.createNativeQuery("SELECT category_id, name FROM category")
                .getResultList();

        return results.stream()
                .map(row -> {
                    Object[] cols = (Object[]) row;
                    Long id = ((Number) cols[0]).longValue();
                    String name = (String) cols[1];
                    List<Long> itemIds = getItemIdsForCategory(id);
                    return new CategoryDTO(id, name, itemIds);
                })
                .toList();
    }

    @Override
    public CategoryDTO findCategoryById(Long categoryId) {
        var result = em.createNativeQuery("SELECT category_id, name FROM category WHERE category_id = :id")
                .setParameter("id", categoryId)
                .getSingleResult();

        Object[] cols = (Object[]) result;
        Long id = ((Number) cols[0]).longValue();
        String name = (String) cols[1];
        List<Long> itemIds = getItemIdsForCategory(id);
        return new CategoryDTO(id, name, itemIds);
    }

    @Override
    public CategoryDTO createCategory(String name) {
        em.createNativeQuery("INSERT INTO category (name) VALUES (:name)")
                .setParameter("name", name)
                .executeUpdate();

        var id = ((Number) em.createNativeQuery("SELECT currval(pg_get_serial_sequence('category', 'category_id'))")
                .getSingleResult()).longValue();

        return new CategoryDTO(id, name, List.of());
    }

    @Override
    public void renameCategory(Long categoryId, String newName) {
        em.createNativeQuery("UPDATE category SET name = :name WHERE category_id = :id")
                .setParameter("name", newName)
                .setParameter("id", categoryId)
                .executeUpdate();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        em.createNativeQuery("DELETE FROM item_category WHERE category_id = :id")
                .setParameter("id", categoryId)
                .executeUpdate();

        em.createNativeQuery("DELETE FROM category WHERE category_id = :id")
                .setParameter("id", categoryId)
                .executeUpdate();
    }

    @Override
    public void addItemToCategory(Long categoryId, Long itemId) {
        try {
            em.createNativeQuery("""
                    INSERT INTO item_category (item_id, category_id)
                    VALUES (:itemId, :categoryId)
                """)//     ON CONFLICT DO NOTHING
                // """)
                .setParameter("itemId", itemId)
                .setParameter("categoryId", categoryId)
                .executeUpdate();
        } catch (Exception e) {
            System.err.println("Error inserting into item_category (itemID=" + itemId + ", categoryID=" + categoryId + "): " + e.getMessage());
            e.printStackTrace(); 
        }
    }
    

    @Override
    public List<Long> getItemIdsForCategory(Long categoryId) {
        return em.createNativeQuery("SELECT item_id FROM item_category WHERE category_id = :id")
                .setParameter("id", categoryId)
                .getResultList()
                .stream()
                .map(id -> ((Number) id).longValue())
                .toList();
    }
    
    @Override
    public List<CategoryUnitTotalDTO> getCategoryTotalsByUnit(Long categoryId) {
        List<Object[]> results = em.createNativeQuery("""
            SELECT i.unit, SUM(i.amount)
            FROM item i
            JOIN item_category ic ON i.item_id = ic.item_id
            WHERE ic.category_id = :categoryId
            GROUP BY i.unit
        """)
        .setParameter("categoryId", categoryId)
        .getResultList();
    
        return results.stream()
            .map(row -> new CategoryUnitTotalDTO(
                (String) row[0],
                ((Number) row[1]).doubleValue()
            )).toList();
    };
    
    @Override
    public List<CategoryTotalDTO> getAllCategoryTotals() {
        List<Object[]> results = em.createNativeQuery("""
            SELECT c.category_id, i.unit, SUM(i.amount)
            FROM item i
            JOIN item_category ic ON i.item_id = ic.item_id
            JOIN category c ON ic.category_id = c.category_id
            GROUP BY c.category_id, i.unit
            ORDER BY c.category_id
        """).getResultList();
    
        // map of categoryID -> list of totals
        Map<Long, List<CategoryUnitTotalDTO>> grouped = new LinkedHashMap<>();
        for (Object[] row : results) {
            Long categoryID = ((Number) row[0]).longValue();
            String unit = (String) row[1];
            Double totalAmount = ((Number) row[2]).doubleValue();
    
            grouped.computeIfAbsent(categoryID, k -> new ArrayList<>())
                    .add(new CategoryUnitTotalDTO(unit, totalAmount));
        }
    
        // wrap into CategoryTotalDTO
        return grouped.entrySet().stream()
            .map(entry -> new CategoryTotalDTO(entry.getKey(), entry.getValue()))
            .toList();
    }
    
}
