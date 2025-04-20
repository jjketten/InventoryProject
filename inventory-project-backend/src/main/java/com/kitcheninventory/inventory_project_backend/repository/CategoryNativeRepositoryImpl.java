package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.CategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
