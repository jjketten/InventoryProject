package com.kitcheninventory.inventory_project_backend.repository;

import com.kitcheninventory.inventory_project_backend.dto.ReminderDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
                    r.itemid as item_id,
                    i.name as item_name,
                    i.brand,
                    i.unit,
                    i.amount,
                    r.purchaseid,
                    p.date,
                    p.store,
                    p.totalcost,
                    r.datetime,
                    r.completed
                FROM reminder r
                JOIN item i ON i.item_id = r.itemid
                JOIN purchase p ON p.purchaseid = r.purchaseid
                """,
                "ReminderDTOMapping"
        );

        return query.getResultList();
    }

    @Override
    public void insertReminder(Long itemID, Long purchaseID, LocalDateTime dateTime, boolean completed) {
        Query query = entityManager.createNativeQuery(
                """
                INSERT INTO reminder (itemid, purchaseid, datetime, completed)
                VALUES (:itemID, :purchaseID, :dateTime, :completed)
                """
        );
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.setParameter("dateTime", dateTime);
        query.setParameter("completed", completed);
        query.executeUpdate();
    }

    @Override
    public void deleteReminder(Long itemID, Long purchaseID) {
        Query query = entityManager.createNativeQuery(
                "DELETE FROM reminder WHERE itemid = :itemID AND purchaseid = :purchaseID"
        );
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.executeUpdate();
    }

    @Override
    public void updateCompletionStatus(Long itemID, Long purchaseID, boolean completed) {
        Query query = entityManager.createNativeQuery(
                "UPDATE reminder SET completed = :completed WHERE itemid = :itemID AND purchaseid = :purchaseID"
        );
        query.setParameter("completed", completed);
        query.setParameter("itemID", itemID);
        query.setParameter("purchaseID", purchaseID);
        query.executeUpdate();
    }
}
