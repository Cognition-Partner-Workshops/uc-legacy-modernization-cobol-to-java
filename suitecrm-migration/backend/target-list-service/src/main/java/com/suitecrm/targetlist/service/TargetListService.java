package com.suitecrm.targetlist.service;

import com.suitecrm.targetlist.dto.*;
import com.suitecrm.targetlist.entity.*;
import com.suitecrm.targetlist.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TargetListService {

    private final TargetListRepository targetListRepository;
    private final TargetRepository targetRepository;
    private final TargetListMemberRepository memberRepository;

    // Target Lists
    public Page<TargetListDto> listTargetLists(Pageable pageable) {
        return targetListRepository.findByDeletedFalse(pageable).map(this::toTargetListDto);
    }

    public Page<TargetListDto> listByType(String listType, Pageable pageable) {
        return targetListRepository.findByListTypeAndDeletedFalse(listType, pageable).map(this::toTargetListDto);
    }

    public TargetListDto getTargetList(UUID id) {
        TargetList list = targetListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target list not found: " + id));
        TargetListDto dto = toTargetListDto(list);
        dto.setEntryCount((int) memberRepository.countByProspectListIdAndDeletedFalse(id));
        return dto;
    }

    public TargetListDto createTargetList(TargetListCreateRequest request, UUID userId) {
        TargetList list = TargetList.builder()
                .name(request.getName())
                .description(request.getDescription())
                .listType(request.getListType() != null ? request.getListType() : "default")
                .domainName(request.getDomainName())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        return toTargetListDto(targetListRepository.save(list));
    }

    public TargetListDto updateTargetList(UUID id, TargetListCreateRequest request) {
        TargetList list = targetListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target list not found: " + id));
        if (request.getName() != null) list.setName(request.getName());
        if (request.getDescription() != null) list.setDescription(request.getDescription());
        if (request.getListType() != null) list.setListType(request.getListType());
        if (request.getDomainName() != null) list.setDomainName(request.getDomainName());
        return toTargetListDto(targetListRepository.save(list));
    }

    public void deleteTargetList(UUID id) {
        TargetList list = targetListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target list not found: " + id));
        list.setDeleted(true);
        targetListRepository.save(list);
    }

    // Members
    public List<TargetListMemberDto> getMembers(UUID listId) {
        return memberRepository.findByProspectListIdAndDeletedFalse(listId)
                .stream().map(this::toMemberDto).collect(Collectors.toList());
    }

    public TargetListMemberDto addMember(UUID listId, TargetListMemberDto request) {
        TargetListMember member = TargetListMember.builder()
                .prospectListId(listId)
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .build();
        TargetListMember saved = memberRepository.save(member);

        // Update entry count
        TargetList list = targetListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("Target list not found: " + listId));
        list.setEntryCount((int) memberRepository.countByProspectListIdAndDeletedFalse(listId));
        targetListRepository.save(list);

        return toMemberDto(saved);
    }

    public void removeMember(UUID memberId) {
        TargetListMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
        member.setDeleted(true);
        memberRepository.save(member);
    }

    // Targets (Prospects)
    public Page<TargetDto> listTargets(Pageable pageable) {
        return targetRepository.findByDeletedFalse(pageable).map(this::toTargetDto);
    }

    public Page<TargetDto> searchTargets(String query, Pageable pageable) {
        return targetRepository.search(query, pageable).map(this::toTargetDto);
    }

    public Page<TargetListDto> searchTargetLists(String query, Pageable pageable) {
        return targetListRepository.search(query, pageable).map(this::toTargetListDto);
    }

    // Mappers
    private TargetListDto toTargetListDto(TargetList list) {
        return TargetListDto.builder()
                .id(list.getId())
                .name(list.getName())
                .description(list.getDescription())
                .listType(list.getListType())
                .domainName(list.getDomainName())
                .entryCount(list.getEntryCount())
                .assignedUserId(list.getAssignedUserId())
                .dateEntered(list.getDateEntered())
                .dateModified(list.getDateModified())
                .build();
    }

    private TargetDto toTargetDto(Target target) {
        return TargetDto.builder()
                .id(target.getId())
                .firstName(target.getFirstName())
                .lastName(target.getLastName())
                .title(target.getTitle())
                .department(target.getDepartment())
                .phoneWork(target.getPhoneWork())
                .phoneMobile(target.getPhoneMobile())
                .emailAddress(target.getEmailAddress())
                .primaryAddressCity(target.getPrimaryAddressCity())
                .primaryAddressState(target.getPrimaryAddressState())
                .primaryAddressCountry(target.getPrimaryAddressCountry())
                .accountName(target.getAccountName())
                .doNotCall(target.getDoNotCall())
                .assignedUserId(target.getAssignedUserId())
                .dateEntered(target.getDateEntered())
                .build();
    }

    private TargetListMemberDto toMemberDto(TargetListMember member) {
        return TargetListMemberDto.builder()
                .id(member.getId())
                .prospectListId(member.getProspectListId())
                .relatedId(member.getRelatedId())
                .relatedType(member.getRelatedType())
                .build();
    }
}
