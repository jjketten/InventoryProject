package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        return entityManager.createNativeQuery("SELECT * FROM full_recipe_view", "RecipeDTOMapping")
            .getResultList();
    }

    @Override
    public Optional<RecipeDTO> getRecipeById(Long id) {
        try {
            RecipeDTO result = (RecipeDTO) entityManager.createNativeQuery("""
                SELECT * FROM full_recipe_view WHERE recipeid = :id
            """, "RecipeDTOMapping")
            .setParameter("id", id)
            .getSingleResult();
            return Optional.of(result);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
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

        entityManager.createNativeQuery("DELETE FROM recipe_item WHERE recipeid = :id")
            .setParameter("id", dto.recipeID())
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM recipe_step WHERE recipeid = :id")
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

        RecipeDTO result = (RecipeDTO)entityManager.createNativeQuery("""
            SELECT * FROM recipe WHERE recipe.id = :id
        """)
        // .setParameter("name", dto.name())
        // .setParameter("reference", dto.reference())
        .setParameter("id", dto.recipeID())
        .getSingleResult();

        return result;
    }
}
