package com.suitecrm.scheduler.repository;
import com.suitecrm.scheduler.entity.Scheduler;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface SchedulerRepository extends JpaRepository<Scheduler, UUID> {
    Page<Scheduler> findByDeletedFalse(Pageable pageable);
    Optional<Scheduler> findByIdAndDeletedFalse(UUID id);
    List<Scheduler> findByStatusAndDeletedFalse(String status);
}
