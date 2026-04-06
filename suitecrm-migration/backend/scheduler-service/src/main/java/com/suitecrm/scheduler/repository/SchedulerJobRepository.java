package com.suitecrm.scheduler.repository;
import com.suitecrm.scheduler.entity.SchedulerJob;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.UUID;
@Repository
public interface SchedulerJobRepository extends JpaRepository<SchedulerJob, UUID> {
    Page<SchedulerJob> findByDeletedFalse(Pageable pageable);
    List<SchedulerJob> findBySchedulerIdAndDeletedFalse(UUID schedulerId);
    List<SchedulerJob> findByStatusAndDeletedFalse(String status);
}
