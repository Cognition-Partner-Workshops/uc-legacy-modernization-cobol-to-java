package com.suitecrm.contact.controller;

import com.suitecrm.contact.entity.Lead;
import com.suitecrm.contact.repository.LeadRepository;
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
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadRepository leadRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','MARKETING','USER')")
    public ResponseEntity<Page<Lead>> listLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Lead> leads;
        if (search != null && !search.isBlank()) {
            leads = leadRepository.searchLeads(search, pageable);
        } else if (status != null && !status.isBlank()) {
            leads = leadRepository.findByStatusAndDeletedFalse(status, pageable);
        } else {
            leads = leadRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','MARKETING','USER')")
    public ResponseEntity<Lead> getLead(@PathVariable UUID id) {
        Lead lead = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return ResponseEntity.ok(lead);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','MARKETING','USER')")
    public ResponseEntity<Lead> createLead(@Valid @RequestBody Lead lead) {
        Lead saved = leadRepository.save(lead);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','MARKETING','USER')")
    public ResponseEntity<Lead> updateLead(@PathVariable UUID id, @Valid @RequestBody Lead leadUpdate) {
        Lead existing = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        existing.setFirstName(leadUpdate.getFirstName());
        existing.setLastName(leadUpdate.getLastName());
        existing.setTitle(leadUpdate.getTitle());
        existing.setCompany(leadUpdate.getCompany());
        existing.setDepartment(leadUpdate.getDepartment());
        existing.setEmailPrimary(leadUpdate.getEmailPrimary());
        existing.setPhoneWork(leadUpdate.getPhoneWork());
        existing.setPhoneMobile(leadUpdate.getPhoneMobile());
        existing.setWebsite(leadUpdate.getWebsite());
        existing.setStatus(leadUpdate.getStatus());
        existing.setLeadSource(leadUpdate.getLeadSource());
        existing.setRating(leadUpdate.getRating());
        existing.setIndustry(leadUpdate.getIndustry());
        existing.setDescription(leadUpdate.getDescription());

        Lead saved = leadRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public ResponseEntity<Lead> convertLead(@PathVariable UUID id) {
        Lead lead = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (lead.getConverted()) {
            throw new RuntimeException("Lead is already converted");
        }

        lead.setConverted(true);
        lead.setStatus("Converted");
        Lead saved = leadRepository.save(lead);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
        Lead lead = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setDeleted(true);
        leadRepository.save(lead);
        return ResponseEntity.noContent().build();
    }
}
