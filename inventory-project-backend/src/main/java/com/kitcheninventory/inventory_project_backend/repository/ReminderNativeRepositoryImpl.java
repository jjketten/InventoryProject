package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ReminderNativeRepositoryImpl implements ReminderNativeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ReminderDTO> findAllReminders() {
        Query query = entityManager.createNativeQuery(
                """
                SELECT 
                    r.item_id as item_id,
                    i.name as item_name,
                    i.brand,
                    i.unit,
                    i.amount,
                    r.purchase_id,
                    p.date,
                    p.store,
                    p.totalcost,
                    r.datetime,
                    r.completed,
                    r.description
                FROM reminder r
                JOIN item i ON i.item_id = r.item_id
                JOIN purchase p ON p.purchase_id = r.purchase_id
                """,
                "ReminderDTOMapping"
        );

        return query.getResultList();
    }

    @Override
    @Transactional
    public void insertReminder(Long itemID, Long purchaseID, OffsetDateTime dateTime, boolean completed, String desc) {
        Query query = entityManager.createNativeQuery(
                """
                INSERT INTO reminder (item_id, purchase_id, datetime, completed, description)
                VALUES (:itemID, :purchaseID, :dateTime, :completed, :desc)
                """
        );
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.setParameter("dateTime", dateTime);
        query.setParameter("completed", completed);
        query.setParameter("desc", desc);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public void deleteReminder(Long itemID, Long purchaseID) {
        Query query = entityManager.createNativeQuery(
                "DELETE FROM reminder WHERE item_id = :itemID AND purchase_id = :purchaseID"
        );
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public void updateCompletionStatus(Long itemID, Long purchaseID, boolean completed) {
        Query query = entityManager.createNativeQuery(
                "UPDATE reminder SET completed = :completed WHERE item_id = :itemID AND purchase_id = :purchaseID"
        );
        query.setParameter("completed", completed);
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.executeUpdate();
    }
}
