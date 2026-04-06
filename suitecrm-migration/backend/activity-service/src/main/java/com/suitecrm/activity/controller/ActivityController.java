package com.suitecrm.activity.controller;

import com.suitecrm.activity.entity.Call;
import com.suitecrm.activity.entity.Meeting;
import com.suitecrm.activity.entity.Note;
import com.suitecrm.activity.entity.Task;
import com.suitecrm.activity.repository.CallRepository;
import com.suitecrm.activity.repository.MeetingRepository;
import com.suitecrm.activity.repository.NoteRepository;
import com.suitecrm.activity.repository.TaskRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final MeetingRepository meetingRepository;
    private final CallRepository callRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;

    // --- Meetings ---
    @GetMapping("/meetings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Meeting>> listMeetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateStart") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(meetingRepository.findByDeletedFalse(pageable));
    }

    @GetMapping("/meetings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Meeting> getMeeting(@PathVariable UUID id) {
        return ResponseEntity.ok(meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found")));
    }

    @PostMapping("/meetings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER')")
    public ResponseEntity<Meeting> createMeeting(@Valid @RequestBody Meeting meeting) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingRepository.save(meeting));
    }

    // --- Calls ---
    @GetMapping("/calls")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER','VIEWER')")
    public ResponseEntity<Page<Call>> listCalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateStart") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(callRepository.findByDeletedFalse(pageable));
    }

    @PostMapping("/calls")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER')")
    public ResponseEntity<Call> createCall(@Valid @RequestBody Call call) {
        return ResponseEntity.status(HttpStatus.CREATED).body(callRepository.save(call));
    }

    // --- Tasks ---
    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Task>> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateDue") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskRepository.findByDeletedFalse(pageable));
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER')")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRepository.save(task));
    }

    // --- Notes ---
    @GetMapping("/notes")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Note>> listNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(noteRepository.findByDeletedFalse(pageable));
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER')")
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteRepository.save(note));
    }
}
