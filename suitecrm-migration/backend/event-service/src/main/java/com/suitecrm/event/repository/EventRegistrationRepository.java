package com.suitecrm.event.repository;

import com.suitecrm.event.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    List<EventRegistration> findByEventIdAndDeletedFalse(UUID eventId);

    List<EventRegistration> findByContactIdAndDeletedFalse(UUID contactId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.deleted = false")
    long countByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.eventId = :eventId AND r.acceptStatus = :status AND r.deleted = false")
    long countByEventIdAndAcceptStatus(@Param("eventId") UUID eventId, @Param("status") String status);
}
