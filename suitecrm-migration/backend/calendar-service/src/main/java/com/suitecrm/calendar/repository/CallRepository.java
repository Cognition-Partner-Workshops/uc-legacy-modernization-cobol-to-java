package com.suitecrm.calendar.repository;

import com.suitecrm.calendar.entity.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, UUID> {
    Page<Call> findByDeletedFalse(Pageable pageable);
    Optional<Call> findByIdAndDeletedFalse(UUID id);
    List<Call> findByAssignedUserIdAndDeletedFalse(UUID userId);
    List<Call> findByDateStartBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<Call> findByStatusAndDeletedFalse(String status);
}
