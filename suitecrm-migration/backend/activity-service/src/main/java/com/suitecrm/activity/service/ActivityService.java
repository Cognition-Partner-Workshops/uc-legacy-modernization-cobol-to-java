package com.suitecrm.activity.service;

import com.suitecrm.activity.dto.*;
import com.suitecrm.activity.entity.*;
import com.suitecrm.activity.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final CallRepository callRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final ReminderRepository reminderRepository;

    // === Calls ===
    @Transactional(readOnly = true)
    public Page<CallDto> listCalls(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return callRepository.findByDeletedFalse(pageable).map(this::toCallDto);
    }

    @Transactional(readOnly = true)
    public CallDto getCall(UUID id) {
        Call call = callRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Call not found with id: " + id));
        return toCallDto(call);
    }

    public CallDto createCall(ActivityCreateRequest request, UUID createdBy) {
        log.info("Creating call: name={}", request.getName());
        Call call = Call.builder()
                .name(request.getName())
                .direction(request.getDirection())
                .status(request.getStatus() != null ? request.getStatus() : "Planned")
                .dateStart(request.getDateStart())
                .dateEnd(request.getDateEnd())
                .durationHours(request.getDurationHours())
                .durationMinutes(request.getDurationMinutes())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .parentType(request.getParentType())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Call saved = callRepository.save(call);
        return toCallDto(saved);
    }

    public void deleteCall(UUID id) {
        Call call = callRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Call not found with id: " + id));
        call.setDeleted(true);
        callRepository.save(call);
    }

    // === Meetings ===
    @Transactional(readOnly = true)
    public Page<MeetingDto> listMeetings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return meetingRepository.findByDeletedFalse(pageable).map(this::toMeetingDto);
    }

    @Transactional(readOnly = true)
    public MeetingDto getMeeting(UUID id) {
        Meeting meeting = meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + id));
        return toMeetingDto(meeting);
    }

    public MeetingDto createMeeting(ActivityCreateRequest request, UUID createdBy) {
        log.info("Creating meeting: name={}", request.getName());
        Meeting meeting = Meeting.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : "Planned")
                .type(request.getType())
                .location(request.getLocation())
                .dateStart(request.getDateStart())
                .dateEnd(request.getDateEnd())
                .durationHours(request.getDurationHours())
                .durationMinutes(request.getDurationMinutes())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .parentType(request.getParentType())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Meeting saved = meetingRepository.save(meeting);
        return toMeetingDto(saved);
    }

    public void deleteMeeting(UUID id) {
        Meeting meeting = meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found with id: " + id));
        meeting.setDeleted(true);
        meetingRepository.save(meeting);
    }

    // === Tasks ===
    @Transactional(readOnly = true)
    public Page<TaskDto> listTasks(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.findByDeletedFalse(pageable).map(this::toTaskDto);
    }

    public TaskDto createTask(ActivityCreateRequest request, UUID createdBy) {
        log.info("Creating task: name={}", request.getName());
        Task task = Task.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : "Not Started")
                .priority(request.getPriority() != null ? request.getPriority() : "Medium")
                .dateStart(request.getDateStart() != null ? request.getDateStart().toLocalDate() : null)
                .dateDue(request.getDateDue())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .parentType(request.getParentType())
                .contactId(request.getContactId())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Task saved = taskRepository.save(task);
        return toTaskDto(saved);
    }

    public void deleteTask(UUID id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        task.setDeleted(true);
        taskRepository.save(task);
    }

    private CallDto toCallDto(Call entity) {
        return CallDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .direction(entity.getDirection())
                .status(entity.getStatus())
                .dateStart(entity.getDateStart())
                .dateEnd(entity.getDateEnd())
                .durationHours(entity.getDurationHours())
                .durationMinutes(entity.getDurationMinutes())
                .description(entity.getDescription())
                .parentId(entity.getParentId())
                .parentType(entity.getParentType())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }

    private MeetingDto toMeetingDto(Meeting entity) {
        return MeetingDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .type(entity.getType())
                .location(entity.getLocation())
                .dateStart(entity.getDateStart())
                .dateEnd(entity.getDateEnd())
                .durationHours(entity.getDurationHours())
                .durationMinutes(entity.getDurationMinutes())
                .description(entity.getDescription())
                .parentId(entity.getParentId())
                .parentType(entity.getParentType())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }

    private TaskDto toTaskDto(Task entity) {
        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .dateDue(entity.getDateDue())
                .dateStart(entity.getDateStart())
                .description(entity.getDescription())
                .parentId(entity.getParentId())
                .parentType(entity.getParentType())
                .contactId(entity.getContactId())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
