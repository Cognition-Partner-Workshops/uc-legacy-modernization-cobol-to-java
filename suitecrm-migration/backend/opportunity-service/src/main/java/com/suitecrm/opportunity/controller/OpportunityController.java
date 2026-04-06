package com.suitecrm.opportunity.controller;

import com.suitecrm.opportunity.entity.Opportunity;
import com.suitecrm.opportunity.repository.OpportunityRepository;
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
@RequestMapping("/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER','VIEWER')")
    public ResponseEntity<Page<Opportunity>> listOpportunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String salesStage) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Opportunity> opportunities;
        if (search != null && !search.isBlank()) {
            opportunities = opportunityRepository.searchOpportunities(search, pageable);
        } else if (salesStage != null && !salesStage.isBlank()) {
            opportunities = opportunityRepository.findBySalesStageAndDeletedFalse(salesStage, pageable);
        } else {
            opportunities = opportunityRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(opportunities);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER','VIEWER')")
    public ResponseEntity<Opportunity> getOpportunity(@PathVariable UUID id) {
        Opportunity opp = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));
        return ResponseEntity.ok(opp);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Opportunity> createOpportunity(@Valid @RequestBody Opportunity opportunity) {
        Opportunity saved = opportunityRepository.save(opportunity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Opportunity> updateOpportunity(@PathVariable UUID id, @Valid @RequestBody Opportunity update) {
        Opportunity existing = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));
        existing.setName(update.getName());
        existing.setAccountId(update.getAccountId());
        existing.setAmount(update.getAmount());
        existing.setDateClosed(update.getDateClosed());
        existing.setSalesStage(update.getSalesStage());
        existing.setProbability(update.getProbability());
        existing.setLeadSource(update.getLeadSource());
        existing.setNextStep(update.getNextStep());
        existing.setDescription(update.getDescription());
        return ResponseEntity.ok(opportunityRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable UUID id) {
        Opportunity opp = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));
        opp.setDeleted(true);
        opportunityRepository.save(opp);
        return ResponseEntity.noContent().build();
    }
}
