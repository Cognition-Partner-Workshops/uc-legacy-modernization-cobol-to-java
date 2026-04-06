package com.suitecrm.scheduler.repository;
import com.suitecrm.scheduler.entity.ResourceCalendar;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.time.LocalDate; import java.util.List; import java.util.UUID;
@Repository
public interface ResourceCalendarRepository extends JpaRepository<ResourceCalendar, UUID> {
    List<ResourceCalendar> findByUserIdAndDeletedFalse(UUID userId);
    List<ResourceCalendar> findByEventDateBetweenAndDeletedFalse(LocalDate start, LocalDate end);
}
