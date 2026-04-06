package com.suitecrm.activity.repository;

import com.suitecrm.activity.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    List<Reminder> findByRelatedEventModuleIdAndDeletedFalse(UUID relatedEventModuleId);
    List<Reminder> findByCreatedByAndDeletedFalse(UUID createdBy);

    @Query("SELECT r FROM Reminder r WHERE r.deleted = false AND r.dateWillExecute <= :now AND r.popup = true")
    List<Reminder> findPendingPopupReminders(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.deleted = false AND r.dateWillExecute <= :now AND r.emailSent = false")
    List<Reminder> findPendingEmailReminders(@Param("now") LocalDateTime now);
}
