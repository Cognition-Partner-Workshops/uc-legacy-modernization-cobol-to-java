package com.suitecrm.calendar.repository;

import com.suitecrm.calendar.entity.MeetingInvitee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingInviteeRepository extends JpaRepository<MeetingInvitee, UUID> {
    List<MeetingInvitee> findByMeetingIdAndDeletedFalse(UUID meetingId);
    List<MeetingInvitee> findByInviteeIdAndDeletedFalse(UUID inviteeId);
}
