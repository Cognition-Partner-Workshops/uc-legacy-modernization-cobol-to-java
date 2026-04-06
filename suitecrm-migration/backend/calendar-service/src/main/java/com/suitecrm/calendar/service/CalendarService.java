package com.suitecrm.calendar.service;

import com.suitecrm.calendar.dto.*;
import com.suitecrm.calendar.entity.*;
import com.suitecrm.calendar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CallRepository callRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingInviteeRepository meetingInviteeRepository;

    public Page<CallDto> getAllCalls(Pageable pageable) {
        return callRepository.findByDeletedFalse(pageable).map(this::toCallDto);
    }

    public CallDto getCallById(UUID id) {
        return toCallDto(callRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Call not found: " + id)));
    }

    @Transactional
    public CallDto createCall(CallCreateRequest req) {
        Call call = Call.builder()
                .name(req.getName()).dateStart(req.getDateStart()).dateEnd(req.getDateEnd())
                .direction(req.getDirection()).status(req.getStatus() != null ? req.getStatus() : "Planned")
                .description(req.getDescription()).durationHours(req.getDurationHours())
                .durationMinutes(req.getDurationMinutes()).parentType(req.getParentType())
                .parentId(req.getParentId()).reminderTime(req.getReminderTime())
                .assignedUserId(req.getAssignedUserId()).build();
        return toCallDto(callRepository.save(call));
    }

    public Page<MeetingDto> getAllMeetings(Pageable pageable) {
        return meetingRepository.findByDeletedFalse(pageable).map(this::toMeetingDto);
    }

    public MeetingDto getMeetingById(UUID id) {
        return toMeetingDto(meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found: " + id)));
    }

    @Transactional
    public MeetingDto createMeeting(MeetingCreateRequest req) {
        Meeting meeting = Meeting.builder()
                .name(req.getName()).dateStart(req.getDateStart()).dateEnd(req.getDateEnd())
                .status(req.getStatus() != null ? req.getStatus() : "Planned")
                .type(req.getType()).location(req.getLocation()).description(req.getDescription())
                .durationHours(req.getDurationHours()).durationMinutes(req.getDurationMinutes())
                .parentType(req.getParentType()).parentId(req.getParentId())
                .reminderTime(req.getReminderTime()).assignedUserId(req.getAssignedUserId()).build();
        return toMeetingDto(meetingRepository.save(meeting));
    }

    public List<CallDto> getCallsByDateRange(LocalDateTime start, LocalDateTime end) {
        return callRepository.findByDateStartBetweenAndDeletedFalse(start, end).stream().map(this::toCallDto).toList();
    }

    public List<MeetingDto> getMeetingsByDateRange(LocalDateTime start, LocalDateTime end) {
        return meetingRepository.findByDateStartBetweenAndDeletedFalse(start, end).stream().map(this::toMeetingDto).toList();
    }

    private CallDto toCallDto(Call c) {
        return CallDto.builder().id(c.getId()).name(c.getName()).dateStart(c.getDateStart()).dateEnd(c.getDateEnd())
                .direction(c.getDirection()).status(c.getStatus()).description(c.getDescription())
                .durationHours(c.getDurationHours()).durationMinutes(c.getDurationMinutes())
                .parentType(c.getParentType()).parentId(c.getParentId())
                .assignedUserId(c.getAssignedUserId()).dateEntered(c.getDateEntered()).dateModified(c.getDateModified()).build();
    }

    private MeetingDto toMeetingDto(Meeting m) {
        return MeetingDto.builder().id(m.getId()).name(m.getName()).dateStart(m.getDateStart()).dateEnd(m.getDateEnd())
                .status(m.getStatus()).type(m.getType()).location(m.getLocation()).description(m.getDescription())
                .durationHours(m.getDurationHours()).durationMinutes(m.getDurationMinutes())
                .parentType(m.getParentType()).parentId(m.getParentId())
                .assignedUserId(m.getAssignedUserId()).dateEntered(m.getDateEntered()).dateModified(m.getDateModified()).build();
    }
}
