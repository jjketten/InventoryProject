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

        // Retrieve generated RecipeID
        Long recipeID = ((Number) entityManager
            .createNativeQuery("SELECT currval(pg_get_serial_sequence('recipe','recipeid'))")
            .getSingleResult()).longValue();

        // Insert recipe items
        for (RecipeItemDTO item : dto.items()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_item (recipeid, itemid, unit, amount, categoryid)
                VALUES (:recipeID, :itemID, :unit, :amount, :categoryID)
            """)
            .setParameter("recipeID", recipeID)
            .setParameter("itemID", item.itemID())
            .setParameter("unit", item.unit())
            .setParameter("amount", item.amount())
            .setParameter("categoryID", item.categoryID())
            .executeUpdate();
        }

        // Insert steps
        for (RecipeStepDTO step : dto.steps()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_step (recipeid, stepnumber, content)
                VALUES (:recipeID, :stepNumber, :content)
            """)
            .setParameter("recipeID", recipeID)
            .setParameter("stepNumber", step.stepNumber())
            .setParameter("content", step.content())
            .executeUpdate();
        }
        return recipeID;
    }

    @Override
    public List<RecipeDTO> getAllRecipes() {
        List<Object[]> results = entityManager.createNativeQuery("""
            SELECT 
                r.recipeid, r.name, r.reference,
                ri.itemid, ri.unit, ri.amount, ri.categoryid,
                rs.stepnumber, rs.content,
                c.name AS category_name
            FROM recipe r
            LEFT JOIN recipe_item ri ON r.recipeid = ri.recipeid
            LEFT JOIN recipe_step rs ON r.recipeid = rs.recipeid
            LEFT JOIN category c ON ri.categoryid = c.categoryid
            ORDER BY r.recipeid, rs.stepnumber
        """).getResultList();

        Map<Long, RecipeDTO> recipeMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Long recipeID = ((Number) row[0]).longValue();

            RecipeDTO dto = recipeMap.computeIfAbsent(recipeID, id -> new RecipeDTO(
                recipeID,
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
                r.recipeid, r.name, r.reference,
                ri.itemid, ri.unit, ri.amount, ri.categoryid,
                rs.stepnumber, rs.content,
                c.name AS category_name
            FROM recipe r
            LEFT JOIN recipe_item ri ON r.recipeid = ri.recipeid
            LEFT JOIN recipe_step rs ON r.recipeid = rs.recipeid
            LEFT JOIN category c ON ri.categoryid = c.categoryid
            WHERE r.recipeid = :id
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
        entityManager.createNativeQuery("DELETE FROM recipe_step WHERE recipeid = :id")
            .setParameter("id", id)
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe_item WHERE recipeid = :id")
            .setParameter("id", id)
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe WHERE recipeid = :id")
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
            WHERE recipeid = :id
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
                INSERT INTO recipe_item (recipeid, itemid, unit, amount, categoryid)
                VALUES (:recipeID, :itemID, :unit, :amount, :categoryID)
            """)
            .setParameter("recipeID", dto.recipeID())
            .setParameter("itemID", item.itemID())
            .setParameter("unit", item.unit())
            .setParameter("amount", item.amount())
            .setParameter("categoryID", item.categoryID())
            .executeUpdate();
        }

        for (RecipeStepDTO step : dto.steps()) {
            entityManager.createNativeQuery("""
                INSERT INTO recipe_step (recipeid, stepnumber, content)
                VALUES (:recipeID, :stepNumber, :content)
            """)
            .setParameter("recipeID", dto.recipeID())
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
