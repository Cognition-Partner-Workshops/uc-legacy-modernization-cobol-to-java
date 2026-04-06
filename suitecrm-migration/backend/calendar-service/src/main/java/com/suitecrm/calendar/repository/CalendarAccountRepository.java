package com.suitecrm.calendar.repository;

import com.suitecrm.calendar.entity.CalendarAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarAccountRepository extends JpaRepository<CalendarAccount, UUID> {
    List<CalendarAccount> findByAssignedUserIdAndDeletedFalse(UUID userId);
}
