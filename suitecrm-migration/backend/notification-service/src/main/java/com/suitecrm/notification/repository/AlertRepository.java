package com.suitecrm.notification.repository;
import com.suitecrm.notification.entity.Alert;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    Page<Alert> findByAssignedUserIdAndDeletedFalse(UUID userId, Pageable pageable);
    Optional<Alert> findByIdAndDeletedFalse(UUID id);
    List<Alert> findByAssignedUserIdAndIsReadFalseAndDeletedFalse(UUID userId);
}
