package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RecipeNativeRepositoryImpl implements RecipeNativeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Long createRecipe(RecipeDTO dto) {
        // Insert into recipe table
        entityManager.createNativeQuery("""
            INSERT INTO recipe (name, reference)
            VALUES (:name, :reference)
        """)
        .setParameter("name", dto.name())
        .setParameter("reference", dto.reference())
        .executeUpdate();

        // Retrieve generated Recipe_ID
        Long recipe_ID = ((Number) entityManager
            .createNativeQuery("SELECT currval(pg_get_serial_sequence('recipe','recipe_id'))")
            .getSingleResult()).longValue();

        // Insert recipe items
        for (RecipeItemDTO item : dto.items()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_item (recipe_id, itemid, unit, amount, categoryid)
                VALUES (:recipe_ID, :itemID, :unit, :amount, :categoryID)
            """)
            .setParameter("recipe_ID", recipe_ID)
            .setParameter("itemID", item.itemID())
            .setParameter("unit", item.unit())
            .setParameter("amount", item.amount())
            .setParameter("categoryID", item.categoryID())
            .executeUpdate();
        }

        // Insert steps
        for (RecipeStepDTO step : dto.steps()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_step (recipe_id, stepnumber, content)
                VALUES (:recipe_ID, :stepNumber, :content)
            """)
            .setParameter("recipe_ID", recipe_ID)
            .setParameter("stepNumber", step.stepNumber())
            .setParameter("content", step.content())
            .executeUpdate();
        }
        return recipe_ID;
    }

    @Override
    public List<RecipeDTO> getAllRecipes() {
        List<Object[]> results = entityManager.createNativeQuery("""
            SELECT 
                r.recipe_id, r.name, r.reference,
                ri.itemid, ri.unit, ri.amount, ri.categoryid,
                rs.stepnumber, rs.content,
                c.name AS category_name
            FROM recipe r
            LEFT JOIN recipe_item ri ON r.recipe_id = ri.recipe_id
            LEFT JOIN recipe_step rs ON r.recipe_id = rs.recipe_id
            LEFT JOIN category c ON ri.categoryid = c.categoryid
            ORDER BY r.recipe_id, rs.stepnumber
        """).getResultList();

        Map<Long, RecipeDTO> recipeMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Long recipe_ID = ((Number) row[0]).longValue();

            RecipeDTO dto = recipeMap.computeIfAbsent(recipe_ID, id -> new RecipeDTO(
                recipe_ID,
                (String) row[1],
                (String) row[2],
                new ArrayList<>(),
                new ArrayList<>()
            ));

            if (row[3] != null) {
                dto.items().add(new RecipeItemDTO(
                    ((Number) row[3]).longValue(),
                    (String) row[4],
                    ((Number) row[5]).intValue(),
                    ((Number) row[6]).longValue(),
                    (String) row[9]
                ));
            }

            if (row[7] != null) {
                dto.steps().add(new RecipeStepDTO(
                    ((Number) row[7]).intValue(),
                    (String) row[8]
                ));
            }
        }

        return new ArrayList<>(recipeMap.values());
    }



    @Override
    public Optional<RecipeDTO> getRecipeById(Long id) {
        List<Object[]> results = entityManager.createNativeQuery("""
            SELECT 
                r.recipe_id, r.name, r.reference,
                ri.itemid, ri.unit, ri.amount, ri.categoryid,
                rs.stepnumber, rs.content,
                c.name AS category_name
            FROM recipe r
            LEFT JOIN recipe_item ri ON r.recipe_id = ri.recipe_id
            LEFT JOIN recipe_step rs ON r.recipe_id = rs.recipe_id
            LEFT JOIN category c ON ri.categoryid = c.categoryid
            WHERE r.recipe_id = :id
            ORDER BY rs.stepnumber
        """)
        .setParameter("id", id)
        .getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] firstRow = results.get(0);
        RecipeDTO dto = new RecipeDTO(
            ((Number) firstRow[0]).longValue(),
            (String) firstRow[1],
            (String) firstRow[2],
            new ArrayList<>(),
            new ArrayList<>()
        );

        for (Object[] row : results) {
            if (row[3] != null) {
                dto.items().add(new RecipeItemDTO(
                    ((Number) row[3]).longValue(),
                    (String) row[4],
                    ((Number) row[5]).intValue(),
                    ((Number) row[6]).longValue(),
                    (String) row[9]
                ));
            }

            if (row[7] != null) {
                dto.steps().add(new RecipeStepDTO(
                    ((Number) row[7]).intValue(),
                    (String) row[8]
                ));
            }
        }

        return Optional.of(dto);
    }

    

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        entityManager.createNativeQuery("DELETE FROM recipe_step WHERE recipe_id = :id")
            .setParameter("id", id)
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe_item WHERE recipe_id = :id")
            .setParameter("id", id)
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe WHERE recipe_id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    @Override
    @Transactional
    //Delete and reinsert strategy
    public RecipeDTO updateRecipe(RecipeDTO dto) {
        // 1. Update recipe basic fields
        entityManager.createNativeQuery("""
            UPDATE recipe
            SET name = :name, reference = :reference
            WHERE recipe_id = :id
        """)
        .setParameter("name", dto.name())
        .setParameter("reference", dto.reference())
        .setParameter("id", dto.recipeID())
        .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe_item WHERE recipe_id = :id")
            .setParameter("id", dto.recipeID())
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe_step WHERE recipe_id = :id")
            .setParameter("id", dto.recipeID())
            .executeUpdate();

        //insert items and then steps separately
        for (RecipeItemDTO item : dto.items()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_item (recipe_id, itemid, unit, amount, categoryid)
                VALUES (:recipe_ID, :itemID, :unit, :amount, :categoryID)
            """)
            .setParameter("recipe_ID", dto.recipeID())
            .setParameter("itemID", item.itemID())
            .setParameter("unit", item.unit())
            .setParameter("amount", item.amount())
            .setParameter("categoryID", item.categoryID())
            .executeUpdate();
        }

        for (RecipeStepDTO step : dto.steps()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_step (recipe_id, stepnumber, content)
                VALUES (:recipe_ID, :stepNumber, :content)
            """)
            .setParameter("recipe_ID", dto.recipeID())
            .setParameter("stepNumber", step.stepNumber())
            .setParameter("content", step.content())
            .executeUpdate();
        }

        //verify by returning dto

        // RecipeDTO result = (RecipeDTO)entityManager.createNativeQuery("""
        //     SELECT * FROM recipe WHERE recipe.id = :id
        // """)
        // // .setParameter("name", dto.name())
        // // .setParameter("reference", dto.reference())
        // .setParameter("id", dto.recipeID())
        // .getSingleResult();

        return getRecipeById(dto.recipeID())
            .orElseThrow(() -> new RuntimeException("Updated recipe not found"));

    }
}
