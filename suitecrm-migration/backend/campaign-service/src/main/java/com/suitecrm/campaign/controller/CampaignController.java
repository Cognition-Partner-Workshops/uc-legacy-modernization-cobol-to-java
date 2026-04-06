package com.suitecrm.campaign.controller;

import com.suitecrm.campaign.entity.Campaign;
import com.suitecrm.campaign.repository.CampaignRepository;
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
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignRepository campaignRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Campaign>> listCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Campaign> campaigns;
        if (search != null && !search.isBlank()) {
            campaigns = campaignRepository.searchCampaigns(search, pageable);
        } else if (status != null && !status.isBlank()) {
            campaigns = campaignRepository.findByStatusAndDeletedFalse(status, pageable);
        } else {
            campaigns = campaignRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING','USER','VIEWER')")
    public ResponseEntity<Campaign> getCampaign(@PathVariable UUID id) {
        Campaign campaign = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        return ResponseEntity.ok(campaign);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING')")
    public ResponseEntity<Campaign> createCampaign(@Valid @RequestBody Campaign campaign) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignRepository.save(campaign));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING')")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable UUID id, @Valid @RequestBody Campaign update) {
        Campaign existing = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        existing.setName(update.getName());
        existing.setCampaignType(update.getCampaignType());
        existing.setStatus(update.getStatus());
        existing.setStartDate(update.getStartDate());
        existing.setEndDate(update.getEndDate());
        existing.setBudget(update.getBudget());
        existing.setExpectedRevenue(update.getExpectedRevenue());
        existing.setObjective(update.getObjective());
        existing.setContent(update.getContent());
        existing.setDescription(update.getDescription());
        return ResponseEntity.ok(campaignRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable UUID id) {
        Campaign campaign = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
        return ResponseEntity.noContent().build();
    }
}
