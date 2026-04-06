package com.suitecrm.calendar.repository;

import com.suitecrm.calendar.entity.CallReschedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CallRescheduleRepository extends JpaRepository<CallReschedule, UUID> {
    List<CallReschedule> findByCallIdAndDeletedFalse(UUID callId);
}
