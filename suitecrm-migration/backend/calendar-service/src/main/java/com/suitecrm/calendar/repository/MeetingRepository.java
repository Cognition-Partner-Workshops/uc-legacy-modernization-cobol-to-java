package com.suitecrm.calendar.repository;

import com.suitecrm.calendar.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    Page<Meeting> findByDeletedFalse(Pageable pageable);
    Optional<Meeting> findByIdAndDeletedFalse(UUID id);
    List<Meeting> findByAssignedUserIdAndDeletedFalse(UUID userId);
    List<Meeting> findByDateStartBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);
    List<Meeting> findByStatusAndDeletedFalse(String status);
}
