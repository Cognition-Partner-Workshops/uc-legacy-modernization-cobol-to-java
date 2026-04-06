package com.suitecrm.campaign.service;

import com.suitecrm.campaign.dto.*;
import com.suitecrm.campaign.entity.Campaign;
import com.suitecrm.campaign.entity.CampaignLog;
import com.suitecrm.campaign.entity.CampaignTracker;
import com.suitecrm.campaign.entity.EmailMarketing;
import com.suitecrm.campaign.repository.CampaignLogRepository;
import com.suitecrm.campaign.repository.CampaignRepository;
import com.suitecrm.campaign.repository.CampaignTrackerRepository;
import com.suitecrm.campaign.repository.EmailMarketingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignLogRepository campaignLogRepository;
    private final CampaignTrackerRepository campaignTrackerRepository;
    private final EmailMarketingRepository emailMarketingRepository;

    @Transactional(readOnly = true)
    public Page<CampaignDto> listCampaigns(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Campaign> campaigns;
        if (search != null && !search.isBlank()) {
            campaigns = campaignRepository.searchCampaigns(search, pageable);
        } else {
            campaigns = campaignRepository.findByDeletedFalse(pageable);
        }
        return campaigns.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CampaignDto getCampaign(UUID id) {
        Campaign campaign = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        return toDto(campaign);
    }

    public CampaignDto createCampaign(CampaignCreateRequest request, UUID createdBy) {
        log.info("Creating campaign: name={}", request.getName());
        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .campaignType(request.getCampaignType())
                .status(request.getStatus() != null ? request.getStatus() : "Planning")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .actualCost(request.getActualCost())
                .expectedRevenue(request.getExpectedRevenue())
                .description(request.getDescription())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Campaign saved = campaignRepository.save(campaign);
        log.info("Created campaign: id={}", saved.getId());
        return toDto(saved);
    }

    public CampaignDto updateCampaign(UUID id, CampaignCreateRequest request) {
        Campaign existing = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        existing.setName(request.getName());
        existing.setCampaignType(request.getCampaignType());
        existing.setStatus(request.getStatus());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setBudget(request.getBudget());
        existing.setActualCost(request.getActualCost());
        existing.setExpectedRevenue(request.getExpectedRevenue());
        existing.setDescription(request.getDescription());
        existing.setAssignedUserId(request.getAssignedUserId());
        Campaign saved = campaignRepository.save(existing);
        return toDto(saved);
    }

    public void deleteCampaign(UUID id) {
        Campaign campaign = campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignLog> getCampaignLogs(UUID campaignId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("activityDate").descending());
        return campaignLogRepository.findByCampaignIdAndDeletedFalse(campaignId, pageable);
    }

    @Transactional(readOnly = true)
    public List<CampaignTracker> getCampaignTrackers(UUID campaignId) {
        return campaignTrackerRepository.findByCampaignIdAndDeletedFalse(campaignId);
    }

    @Transactional(readOnly = true)
    public List<EmailMarketing> getCampaignEmailMarketing(UUID campaignId) {
        return emailMarketingRepository.findByCampaignIdAndDeletedFalse(campaignId);
    }

    public void logCampaignActivity(UUID campaignId, String activityType, UUID targetId, String targetType) {
        CampaignLog logEntry = CampaignLog.builder()
                .campaignId(campaignId)
                .activityType(activityType)
                .targetId(targetId)
                .targetType(targetType)
                .build();
        campaignLogRepository.save(logEntry);
    }

    private CampaignDto toDto(Campaign entity) {
        return CampaignDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .campaignType(entity.getCampaignType())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .budget(entity.getBudget())
                .description(entity.getDescription())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
