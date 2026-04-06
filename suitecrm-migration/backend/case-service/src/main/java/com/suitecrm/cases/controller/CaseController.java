package com.suitecrm.cases.controller;

import com.suitecrm.cases.entity.SupportCase;
import com.suitecrm.cases.repository.SupportCaseRepository;
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
@RequestMapping("/cases")
@RequiredArgsConstructor
public class CaseController {

    private final SupportCaseRepository caseRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT','USER','VIEWER')")
    public ResponseEntity<Page<SupportCase>> listCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SupportCase> cases;
        if (search != null && !search.isBlank()) {
            cases = caseRepository.searchCases(search, pageable);
        } else if (status != null && !status.isBlank()) {
            cases = caseRepository.findByStatusAndDeletedFalse(status, pageable);
        } else if (priority != null && !priority.isBlank()) {
            cases = caseRepository.findByPriorityAndDeletedFalse(priority, pageable);
        } else {
            cases = caseRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT','USER','VIEWER')")
    public ResponseEntity<SupportCase> getCase(@PathVariable UUID id) {
        SupportCase supportCase = caseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        return ResponseEntity.ok(supportCase);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT','USER')")
    public ResponseEntity<SupportCase> createCase(@Valid @RequestBody SupportCase supportCase) {
        SupportCase saved = caseRepository.save(supportCase);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPPORT','USER')")
    public ResponseEntity<SupportCase> updateCase(@PathVariable UUID id, @Valid @RequestBody SupportCase update) {
        SupportCase existing = caseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        existing.setName(update.getName());
        existing.setStatus(update.getStatus());
        existing.setPriority(update.getPriority());
        existing.setType(update.getType());
        existing.setDescription(update.getDescription());
        existing.setResolution(update.getResolution());
        existing.setWorkLog(update.getWorkLog());
        existing.setAccountId(update.getAccountId());
        existing.setContactId(update.getContactId());
        return ResponseEntity.ok(caseRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteCase(@PathVariable UUID id) {
        SupportCase supportCase = caseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));
        supportCase.setDeleted(true);
        caseRepository.save(supportCase);
        return ResponseEntity.noContent().build();
    }
}
