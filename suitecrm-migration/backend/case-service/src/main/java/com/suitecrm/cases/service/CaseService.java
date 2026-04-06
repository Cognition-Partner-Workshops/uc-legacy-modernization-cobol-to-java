package com.suitecrm.cases.service;

import com.suitecrm.cases.dto.*;
import com.suitecrm.cases.entity.CaseEvent;
import com.suitecrm.cases.entity.CaseUpdate;
import com.suitecrm.cases.entity.SupportCase;
import com.suitecrm.cases.repository.CaseEventRepository;
import com.suitecrm.cases.repository.CaseUpdateRepository;
import com.suitecrm.cases.repository.SupportCaseRepository;
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
public class CaseService {

    private final SupportCaseRepository supportCaseRepository;
    private final CaseEventRepository caseEventRepository;
    private final CaseUpdateRepository caseUpdateRepository;

    @Transactional(readOnly = true)
    public Page<CaseDto> listCases(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SupportCase> cases;
        if (search != null && !search.isBlank()) {
            cases = supportCaseRepository.searchCases(search, pageable);
        } else {
            cases = supportCaseRepository.findByDeletedFalse(pageable);
        }
        return cases.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CaseDto getCase(UUID id) {
        SupportCase sc = supportCaseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + id));
        return toDto(sc);
    }

    public CaseDto createCase(CaseCreateRequest request, UUID createdBy) {
        log.info("Creating case: name={}", request.getName());
        SupportCase sc = SupportCase.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : "New")
                .priority(request.getPriority() != null ? request.getPriority() : "Medium")
                .type(request.getType())
                .description(request.getDescription())
                .resolution(request.getResolution())
                .accountId(request.getAccountId())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        SupportCase saved = supportCaseRepository.save(sc);

        CaseEvent event = CaseEvent.builder()
                .caseId(saved.getId())
                .eventType("CREATED")
                .description("Case created")
                .createdBy(createdBy)
                .build();
        caseEventRepository.save(event);

        log.info("Created case: id={}, number={}", saved.getId(), saved.getCaseNumber());
        return toDto(saved);
    }

    public CaseDto updateCase(UUID id, CaseCreateRequest request, UUID updatedBy) {
        SupportCase existing = supportCaseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + id));
        String oldStatus = existing.getStatus();
        existing.setName(request.getName());
        existing.setStatus(request.getStatus());
        existing.setPriority(request.getPriority());
        existing.setType(request.getType());
        existing.setDescription(request.getDescription());
        existing.setResolution(request.getResolution());
        existing.setAssignedUserId(request.getAssignedUserId());
        SupportCase saved = supportCaseRepository.save(existing);

        if (!oldStatus.equals(request.getStatus())) {
            CaseEvent event = CaseEvent.builder()
                    .caseId(saved.getId())
                    .eventType("STATUS_CHANGED")
                    .description("Status changed from " + oldStatus + " to " + request.getStatus())
                    .createdBy(updatedBy)
                    .build();
            caseEventRepository.save(event);
        }

        return toDto(saved);
    }

    public void addCaseUpdate(UUID caseId, String description, boolean internal, UUID createdBy) {
        CaseUpdate update = CaseUpdate.builder()
                .caseId(caseId)
                .description(description)
                .internal(internal)
                .createdBy(createdBy)
                .build();
        caseUpdateRepository.save(update);
    }

    @Transactional(readOnly = true)
    public List<CaseUpdate> getCaseUpdates(UUID caseId) {
        return caseUpdateRepository.findByCaseIdAndDeletedFalseOrderByDateEnteredDesc(caseId);
    }

    @Transactional(readOnly = true)
    public List<CaseEvent> getCaseEvents(UUID caseId) {
        return caseEventRepository.findByCaseIdAndDeletedFalseOrderByDateEnteredDesc(caseId);
    }

    public void deleteCase(UUID id) {
        SupportCase sc = supportCaseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + id));
        sc.setDeleted(true);
        supportCaseRepository.save(sc);
    }

    private CaseDto toDto(SupportCase entity) {
        return CaseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .caseNumber(entity.getCaseNumber())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .type(entity.getType())
                .description(entity.getDescription())
                .resolution(entity.getResolution())
                .accountId(entity.getAccountId())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
