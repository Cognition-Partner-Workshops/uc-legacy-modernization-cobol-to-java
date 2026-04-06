package com.suitecrm.opportunity.service;

import com.suitecrm.opportunity.dto.*;
import com.suitecrm.opportunity.entity.Opportunity;
import com.suitecrm.opportunity.repository.OpportunityRepository;
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
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    @Transactional(readOnly = true)
    public Page<OpportunityDto> listOpportunities(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Opportunity> opportunities;
        if (search != null && !search.isBlank()) {
            opportunities = opportunityRepository.searchOpportunities(search, pageable);
        } else {
            opportunities = opportunityRepository.findByDeletedFalse(pageable);
        }
        return opportunities.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public OpportunityDto getOpportunity(UUID id) {
        Opportunity opp = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
        return toDto(opp);
    }

    public OpportunityDto createOpportunity(OpportunityCreateRequest request, UUID createdBy) {
        log.info("Creating opportunity: name={}", request.getName());
        Opportunity opp = Opportunity.builder()
                .name(request.getName())
                .accountId(request.getAccountId())
                .salesStage(request.getSalesStage() != null ? request.getSalesStage() : "Prospecting")
                .amount(request.getAmount())
                .currencyId(request.getCurrencyId() != null ? request.getCurrencyId().toString() : null)
                .probability(request.getProbability())
                .dateClosed(request.getDateClosed())
                .nextStep(request.getNextStep())
                .leadSource(request.getLeadSource())
                .opportunityType(request.getOpportunityType())
                .description(request.getDescription())
                .campaignId(request.getCampaignId())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Opportunity saved = opportunityRepository.save(opp);
        log.info("Created opportunity: id={}", saved.getId());
        return toDto(saved);
    }

    public OpportunityDto updateOpportunity(UUID id, OpportunityCreateRequest request) {
        log.info("Updating opportunity: id={}", id);
        Opportunity existing = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
        existing.setName(request.getName());
        existing.setAccountId(request.getAccountId());
        existing.setSalesStage(request.getSalesStage());
        existing.setAmount(request.getAmount());
        existing.setProbability(request.getProbability());
        existing.setDateClosed(request.getDateClosed());
        existing.setNextStep(request.getNextStep());
        existing.setLeadSource(request.getLeadSource());
        existing.setOpportunityType(request.getOpportunityType());
        existing.setDescription(request.getDescription());
        existing.setAssignedUserId(request.getAssignedUserId());
        Opportunity saved = opportunityRepository.save(existing);
        return toDto(saved);
    }

    public void deleteOpportunity(UUID id) {
        log.info("Soft-deleting opportunity: id={}", id);
        Opportunity opp = opportunityRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
        opp.setDeleted(true);
        opportunityRepository.save(opp);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityDto> getByAccount(UUID accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateClosed").ascending());
        return opportunityRepository.findByAccountIdAndDeletedFalse(accountId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityDto> getBySalesStage(String salesStage, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("amount").descending());
        return opportunityRepository.findBySalesStageAndDeletedFalse(salesStage, pageable).map(this::toDto);
    }

    private OpportunityDto toDto(Opportunity entity) {
        return OpportunityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .accountId(entity.getAccountId())
                .salesStage(entity.getSalesStage())
                .amount(entity.getAmount())
                .probability(entity.getProbability())
                .dateClosed(entity.getDateClosed())
                .nextStep(entity.getNextStep())
                .leadSource(entity.getLeadSource())
                .opportunityType(entity.getOpportunityType())
                .description(entity.getDescription())
                .campaignId(entity.getCampaignId())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
