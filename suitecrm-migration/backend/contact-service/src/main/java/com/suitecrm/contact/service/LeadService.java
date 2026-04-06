package com.suitecrm.contact.service;

import com.suitecrm.contact.dto.*;
import com.suitecrm.contact.entity.Lead;
import com.suitecrm.contact.repository.LeadRepository;
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
public class LeadService {

    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public Page<LeadDto> listLeads(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Lead> leads;
        if (search != null && !search.isBlank()) {
            leads = leadRepository.searchLeads(search, pageable);
        } else {
            leads = leadRepository.findByDeletedFalse(pageable);
        }
        return leads.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public LeadDto getLead(UUID id) {
        Lead lead = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
        return toDto(lead);
    }

    public LeadDto createLead(LeadCreateRequest request, UUID createdBy) {
        log.info("Creating lead: {} {}", request.getFirstName(), request.getLastName());
        Lead lead = Lead.builder()
                .salutation(request.getSalutation())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .department(request.getDepartment())
                .company(request.getCompany())
                .phoneWork(request.getPhoneWork())
                .phoneMobile(request.getPhoneMobile())
                .phoneHome(request.getPhoneHome())
                .phoneFax(request.getPhoneFax())
                .email(request.getEmail())
                .status(request.getStatus() != null ? request.getStatus() : "New")
                .statusDescription(request.getStatusDescription())
                .leadSource(request.getLeadSource())
                .leadSourceDescription(request.getLeadSourceDescription())
                .opportunityAmount(request.getOpportunityAmount())
                .referedBy(request.getReferedBy())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .primaryAddressStreet(request.getPrimaryAddressStreet())
                .primaryAddressCity(request.getPrimaryAddressCity())
                .primaryAddressState(request.getPrimaryAddressState())
                .primaryAddressPostalcode(request.getPrimaryAddressPostalcode())
                .primaryAddressCountry(request.getPrimaryAddressCountry())
                .altAddressStreet(request.getAltAddressStreet())
                .altAddressCity(request.getAltAddressCity())
                .altAddressState(request.getAltAddressState())
                .altAddressPostalcode(request.getAltAddressPostalcode())
                .altAddressCountry(request.getAltAddressCountry())
                .description(request.getDescription())
                .assignedUserId(request.getAssignedUserId())
                .campaignId(request.getCampaignId())
                .createdBy(createdBy)
                .build();
        Lead saved = leadRepository.save(lead);
        log.info("Created lead: id={}", saved.getId());
        return toDto(saved);
    }

    public LeadDto convertLead(UUID leadId, LeadConvertRequest request) {
        log.info("Converting lead: id={}", leadId);
        Lead lead = leadRepository.findByIdAndDeletedFalse(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));
        lead.setConverted(true);
        lead.setStatus("Converted");
        Lead saved = leadRepository.save(lead);
        log.info("Converted lead: id={}", saved.getId());
        return toDto(saved);
    }

    public void deleteLead(UUID id) {
        log.info("Soft-deleting lead: id={}", id);
        Lead lead = leadRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
        lead.setDeleted(true);
        leadRepository.save(lead);
    }

    @Transactional(readOnly = true)
    public long countLeads() {
        return leadRepository.countByDeletedFalse();
    }

    private LeadDto toDto(Lead entity) {
        return LeadDto.builder()
                .id(entity.getId())
                .salutation(entity.getSalutation())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .fullName((entity.getFirstName() != null ? entity.getFirstName() + " " : "") + entity.getLastName())
                .title(entity.getTitle())
                .department(entity.getDepartment())
                .company(entity.getCompany())
                .phoneWork(entity.getPhoneWork())
                .phoneMobile(entity.getPhoneMobile())
                .phoneHome(entity.getPhoneHome())
                .phoneFax(entity.getPhoneFax())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .statusDescription(entity.getStatusDescription())
                .leadSource(entity.getLeadSource())
                .leadSourceDescription(entity.getLeadSourceDescription())
                .opportunityAmount(entity.getOpportunityAmount())
                .referedBy(entity.getReferedBy())
                .website(entity.getWebsite())
                .industry(entity.getIndustry())
                .primaryAddressStreet(entity.getPrimaryAddressStreet())
                .primaryAddressCity(entity.getPrimaryAddressCity())
                .primaryAddressState(entity.getPrimaryAddressState())
                .primaryAddressPostalcode(entity.getPrimaryAddressPostalcode())
                .primaryAddressCountry(entity.getPrimaryAddressCountry())
                .altAddressStreet(entity.getAltAddressStreet())
                .altAddressCity(entity.getAltAddressCity())
                .altAddressState(entity.getAltAddressState())
                .altAddressPostalcode(entity.getAltAddressPostalcode())
                .altAddressCountry(entity.getAltAddressCountry())
                .description(entity.getDescription())
                .converted(entity.getConverted())
                .assignedUserId(entity.getAssignedUserId())
                .campaignId(entity.getCampaignId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
