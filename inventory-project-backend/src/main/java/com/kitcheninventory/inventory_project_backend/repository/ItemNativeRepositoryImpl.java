package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ItemDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemNativeRepositoryImpl implements ItemNativeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public ItemDTO saveItemWithCategories(ItemDTO dto) {
        em.createNativeQuery("""
            INSERT INTO item (name, brand, unit, amount)
            VALUES (:name, :brand, :unit, :amount)
        """)
        .setParameter("name", dto.name())
        .setParameter("brand", dto.brand())
        .setParameter("unit", dto.unit())
        .setParameter("amount", dto.amount())
        .executeUpdate();

        Long itemId = ((Number) em.createNativeQuery("SELECT LASTVAL()").getSingleResult()).longValue();

        for (String categoryName : dto.categories()) {
            Long catId = getOrCreateCategoryId(categoryName);
            em.createNativeQuery("INSERT INTO item_category (item_id, category_id) VALUES (:itemId, :catId)")
              .setParameter("itemId", itemId)
              .setParameter("catId", catId)
              .executeUpdate();
        }

        return new ItemDTO(itemId, dto.name(), dto.brand(), dto.unit(), dto.amount(), dto.categories());
    }

    @Transactional
    public ItemDTO saveItemWithCategoriesWithID(ItemDTO dto) {
        em.createNativeQuery("""
            INSERT INTO item (item_id, name, brand, unit, amount)
            VALUES (:itemid, :name, :brand, :unit, :amount)
        """)
        .setParameter("itemid", dto.itemID())
        .setParameter("name", dto.name())
        .setParameter("brand", dto.brand())
        .setParameter("unit", dto.unit())
        .setParameter("amount", dto.amount())
        .executeUpdate();
        
        
        Long itemId = dto.itemID();

        for (String categoryName : dto.categories()) {
            Long catId = getOrCreateCategoryId(categoryName);
            em.createNativeQuery("INSERT INTO item_category (item_id, category_id) VALUES (:itemId, :catId)")
              .setParameter("itemId", itemId)
              .setParameter("catId", catId)
              .executeUpdate();
        }

        return new ItemDTO(itemId, dto.name(), dto.brand(), dto.unit(), dto.amount(), dto.categories());
    }

    @Override
    public List<ItemDTO> getAllItemsWithCategories() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.item_id, i.name, i.brand, i.unit, i.amount, c.name AS category
            FROM item i
            LEFT JOIN item_category ic ON i.item_id = ic.item_id
            LEFT JOIN category c ON ic.category_id = c.category_id
        """).getResultList();

        Map<Long, ItemDTOBuilder> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            map.putIfAbsent(id, new ItemDTOBuilder(id, (String) row[1], (String) row[2], (String) row[3], ((Number) row[4]).intValue()));
            if (row[5] != null) {
                map.get(id).addCategory((String) row[5]);
            }
        }
        return map.values().stream().map(ItemDTOBuilder::build).collect(Collectors.toList());
    }

    @Override
    public Optional<ItemDTO> getItemWithCategoriesById(Long id) {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT i.item_id, i.name, i.brand, i.unit, i.amount, c.name AS category
            FROM item i
            LEFT JOIN item_category ic ON i.item_id = ic.item_id
            LEFT JOIN category c ON ic.category_id = c.category_id
            WHERE i.item_id = :id
        """)
        .setParameter("id", id)
        .getResultList();

        if (rows.isEmpty()) return Optional.empty();

        Object[] first = rows.get(0);
        ItemDTOBuilder builder = new ItemDTOBuilder(
            ((Number) first[0]).longValue(),
            (String) first[1],
            (String) first[2],
            (String) first[3],
            ((Number) first[4]).intValue()
        );
        for (Object[] row : rows) {
            if (row[5] != null) builder.addCategory((String) row[5]);
        }
        return Optional.of(builder.build());
    }

    @Override
    @Transactional
    public void deleteItemAndAssociations(Long id) {
        em.createNativeQuery("DELETE FROM item_category WHERE item_id = :id").setParameter("id", id).executeUpdate();
        em.createNativeQuery("DELETE FROM item WHERE item_id = :id").setParameter("id", id).executeUpdate();
    }

    private Long getOrCreateCategoryId(String name) {
        List<?> res = em.createNativeQuery("SELECT category_id FROM category WHERE name = :name")
                .setParameter("name", name)
                .getResultList();
        if (!res.isEmpty()) {
            return ((Number) res.get(0)).longValue();
        }

        em.createNativeQuery("INSERT INTO category (name) VALUES (:name)").setParameter("name", name).executeUpdate();
        return ((Number) em.createNativeQuery("SELECT LASTVAL()").getSingleResult()).longValue();
    }

    private static class ItemDTOBuilder {
        private final Long id;
        private final String name, brand, unit;
        private final float amount;
        private final List<String> categories = new ArrayList<>();

        ItemDTOBuilder(Long id, String name, String brand, String unit, float amount) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.unit = unit;
            this.amount = amount;
        }

        void addCategory(String c) {
            categories.add(c);
        }

        ItemDTO build() {
            return new ItemDTO(id, name, brand, unit, amount, categories);
        }
    }

    @Override
    public void updateItem(Long id, String name, String brand, String unit, float amount) {
        String sql = """
            UPDATE item SET name = :name, brand = :brand, unit = :unit, amount = :amount
            WHERE item_id = :id
        """;
        em.createNativeQuery(sql)
            .setParameter("id", id)
            .setParameter("name", name)
            .setParameter("brand", brand)
            .setParameter("unit", unit)
            .setParameter("amount", amount)
            .executeUpdate();
    }

    @Override
    public void clearItemCategories(Long itemId) {
        em.createNativeQuery("DELETE FROM item_category WHERE item_id = :id")
            .setParameter("id", itemId)
            .executeUpdate();
    }

    @Override
    @Transactional
    public void addItemCategoriesByName(Long itemId, List<String> categoryNames) {
        for (String name : categoryNames) {
            // 1. Check if category exists
            String categoryIdQuery = "SELECT category_id FROM category WHERE name = :name";
            List<Long> result = em.createNativeQuery(categoryIdQuery)
                    .setParameter("name", name)
                    .getResultList();
    
            Long categoryId;
    
            if (result.isEmpty()) {
                // 2. Insert new category if it doesn't exist
                String insertCategory = "INSERT INTO category(name) VALUES (:name) RETURNING category_id";
                categoryId = ((Number) em.createNativeQuery(insertCategory)
                        .setParameter("name", name)
                        .getSingleResult()).longValue();
            } else {
                categoryId = ((Number) result.get(0)).longValue();
            }
    
            // 3. Insert relation into ItemCategory (ignore duplicates)
            String insertRelation = """
                INSERT INTO item_category(item_id, category_id)
                SELECT :itemId, :categoryId
                WHERE NOT EXISTS (
                    SELECT 1 FROM item_category WHERE item_id = :itemId AND category_id = :categoryId
                )
            """;
    
            em.createNativeQuery(insertRelation)
                    .setParameter("itemId", itemId)
                    .setParameter("categoryId", categoryId)
                    .executeUpdate();
        }
    }
    
    @Override
    public Optional<ItemDTO> findItemById(Long itemId) {
        String itemQuery = """
            SELECT item_id, name, brand, unit, amount
            FROM item
            WHERE item_id = :itemId
        """;

        List<Object[]> items = em.createNativeQuery(itemQuery)
            .setParameter("itemId", itemId)
            .getResultList();

        if (items.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = items.get(0);
        Long id = ((Number) row[0]).longValue();
        String name = (String) row[1];
        String brand = (String) row[2];
        String unit = (String) row[3];
        float amount = ((Number) row[4]).intValue();

        // Fetch categories for the item
        String categoryQuery = """
            SELECT c.name
            FROM category c
            JOIN item_category ic ON c.category_id = ic.category_id
            WHERE ic.item_id = :itemId
        """;

        List<String> categories = em.createNativeQuery(categoryQuery)
            .setParameter("itemId", itemId)
            .getResultList();

        ItemDTO dto = new ItemDTO(id, name, brand, unit, amount, categories);
        return Optional.of(dto);
    }

    @Override
    @SuppressWarnings("unchecked")
    //Returns first item with matching name
    public Optional<ItemDTO> findItemByName(String name) {
        String itemQuery = """
            SELECT item_id, name, brand, unit, amount
            FROM item
            WHERE name = :name
            LIMIT 1
        """;

        List<Object[]> items = em.createNativeQuery(itemQuery)
            .setParameter("name", name)
            .getResultList();

        if (items.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = items.get(0);
        Long id = ((Number) row[0]).longValue();
        String itemName = (String) row[1];
        String brand = (String) row[2];
        String unit = (String) row[3];
        float amount = ((Number) row[4]).floatValue();

        // Fetch categories for the item
        String categoryQuery = """
            SELECT c.name
            FROM category c
            JOIN item_category ic ON c.category_id = ic.category_id
            WHERE ic.item_id = :itemId
        """;

        List<String> categories = em.createNativeQuery(categoryQuery)
            .setParameter("itemId", id)
            .getResultList();

        ItemDTO dto = new ItemDTO(id, itemName, brand, unit, amount, categories);
        return Optional.of(dto);
    }

    @Override
    @SuppressWarnings("unchecked")
    //Same as above but returns all matches
    public List<ItemDTO> findItemsByName(String name) {
        String itemQuery = """
            SELECT item_id, name, brand, unit, amount
            FROM item
            WHERE name = :name
        """;

        List<Object[]> rows = em.createNativeQuery(itemQuery)
            .setParameter("name", name)
            .getResultList();

        List<ItemDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String itemName = (String) row[1];
            String brand = (String) row[2];
            String unit = (String) row[3];
            float amount = ((Number) row[4]).floatValue();

            // Fetch categories
            List<String> categories = em.createNativeQuery("""
                SELECT c.name
                FROM category c
                JOIN item_category ic ON c.category_id = ic.category_id
                WHERE ic.item_id = :itemId
            """)
            .setParameter("itemId", id)
            .getResultList();

            result.add(new ItemDTO(id, itemName, brand, unit, amount, categories));
        }

        return result;
    }



}
