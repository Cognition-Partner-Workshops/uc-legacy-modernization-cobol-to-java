package com.suitecrm.scheduler.repository;
import com.suitecrm.scheduler.entity.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.UUID;
@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    List<BusinessHours> findByDeletedFalseOrderByDayOfWeekAsc();
    List<BusinessHours> findByDayOfWeekAndDeletedFalse(Integer dayOfWeek);
}
