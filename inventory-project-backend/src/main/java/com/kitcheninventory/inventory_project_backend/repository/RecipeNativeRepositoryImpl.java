package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

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
                INSERT INTO recipe_item (recipe_id, item_id, unit, amount, category_id)
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
        // Step 1: Fetch all basic recipe info
        List<Object[]> recipeRows = entityManager.createNativeQuery("""
            SELECT recipe_id, name, reference
            FROM recipe
            ORDER BY recipe_id
        """).getResultList();

        // Step 2: Fetch all recipe items
        List<Object[]> itemRows = entityManager.createNativeQuery("""
            SELECT ri.recipe_id, ri.item_id, ri.unit, ri.amount, ri.category_id, c.name AS category_name
            FROM recipe_item ri
            LEFT JOIN category c ON ri.category_id = c.category_id
        """).getResultList();

        // Step 3: Fetch all recipe steps
        List<Object[]> stepRows = entityManager.createNativeQuery("""
            SELECT recipe_id, stepnumber, content
            FROM recipe_step
            ORDER BY recipe_id, stepnumber
        """).getResultList();

        // Step 4: Build recipe DTOs
        Map<Long, RecipeDTO> recipeMap = new LinkedHashMap<>();

        for (Object[] row : recipeRows) {
            Long recipeID = ((Number) row[0]).longValue();
            recipeMap.put(recipeID, new RecipeDTO(
                recipeID,
                (String) row[1],
                (String) row[2],
                new ArrayList<>(),
                new ArrayList<>()
            ));
        }

        for (Object[] row : itemRows) {
            Long recipeID = ((Number) row[0]).longValue();
            RecipeDTO dto = recipeMap.get(recipeID);
            if (dto != null) {
                dto.items().add(new RecipeItemDTO(
                    ((Number) row[1]).longValue(),
                    (String) row[2],
                    ((Number) row[3]).intValue(),
                    row[4] != null ? ((Number) row[4]).longValue() : null,
                    (String) row[5]
                ));
            }
        }

        for (Object[] row : stepRows) {
            Long recipeID = ((Number) row[0]).longValue();
            RecipeDTO dto = recipeMap.get(recipeID);
            if (dto != null) {
                dto.steps().add(new RecipeStepDTO(
                    ((Number) row[1]).intValue(),
                    (String) row[2]
                ));
            }
        }

        return new ArrayList<>(recipeMap.values());
    }




    @Override
    public Optional<RecipeDTO> getRecipeById(Long id) {
        //fetch basic recipe info
        Object[] recipeRow = (Object[]) entityManager.createNativeQuery("""
            SELECT recipe_id, name, reference
            FROM recipe
            WHERE recipe_id = :id
        """)
        .setParameter("id", id)
        .getSingleResult();

        if (recipeRow == null) {
            return Optional.empty();
        }

        Long recipeID = ((Number) recipeRow[0]).longValue();
        String name = (String) recipeRow[1];
        String reference = (String) recipeRow[2];

        //fetch recipe items
        List<Object[]> itemRows = entityManager.createNativeQuery("""
            SELECT ri.item_id, ri.unit, ri.amount, ri.category_id, c.name AS category_name
            FROM recipe_item ri
            LEFT JOIN category c ON ri.category_id = c.category_id
            WHERE ri.recipe_id = :id
        """)
        .setParameter("id", id)
        .getResultList();

        List<RecipeItemDTO> items = new ArrayList<>();
        for (Object[] row : itemRows) {
            items.add(new RecipeItemDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).intValue(),
                row[3] != null ? ((Number) row[3]).longValue() : null,
                (String) row[4]
            ));
        }

        //fetch steps
        List<Object[]> stepRows = entityManager.createNativeQuery("""
            SELECT stepnumber, content
            FROM recipe_step
            WHERE recipe_id = :id
            ORDER BY stepnumber
        """)
        .setParameter("id", id)
        .getResultList();

        List<RecipeStepDTO> steps = new ArrayList<>();
        for (Object[] row : stepRows) {
            steps.add(new RecipeStepDTO(
                ((Number) row[0]).intValue(),
                (String) row[1]
            ));
        }

        RecipeDTO recipe = new RecipeDTO(recipeID, name, reference, items, steps);
        return Optional.of(recipe);
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
                INSERT INTO recipe_item (recipe_id, item_id, unit, amount, category_id)
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
