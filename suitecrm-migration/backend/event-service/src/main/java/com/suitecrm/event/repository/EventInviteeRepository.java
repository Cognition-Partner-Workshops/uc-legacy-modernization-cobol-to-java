package com.suitecrm.event.repository;

import com.suitecrm.event.entity.EventInvitee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventInviteeRepository extends JpaRepository<EventInvitee, UUID> {

    List<EventInvitee> findByEventIdAndDeletedFalse(UUID eventId);

    List<EventInvitee> findByInviteeIdAndDeletedFalse(UUID inviteeId);

    List<EventInvitee> findByEventIdAndAcceptStatusAndDeletedFalse(UUID eventId, String acceptStatus);
}
