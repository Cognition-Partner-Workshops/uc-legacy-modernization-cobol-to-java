package com.suitecrm.event.service;

import com.suitecrm.event.dto.*;
import com.suitecrm.event.entity.*;
import com.suitecrm.event.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final FPEventRepository eventRepository;
    private final FPEventLocationRepository locationRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventInviteeRepository inviteeRepository;

    public Page<FPEventDto> listEvents(Pageable pageable) {
        return eventRepository.findByDeletedFalse(pageable).map(this::toEventDto);
    }

    public FPEventDto getEvent(UUID id) {
        FPEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        FPEventDto dto = toEventDto(event);
        dto.setRegistrationCount(registrationRepository.countByEventId(id));
        dto.setAcceptedCount(registrationRepository.countByEventIdAndAcceptStatus(id, "accepted"));
        return dto;
    }

    public FPEventDto createEvent(FPEventCreateRequest request, UUID userId) {
        FPEvent event = FPEvent.builder()
                .name(request.getName())
                .description(request.getDescription())
                .dateStart(request.getDateStart())
                .dateEnd(request.getDateEnd())
                .durationHours(request.getDurationHours())
                .durationMinutes(request.getDurationMinutes())
                .status(request.getStatus() != null ? request.getStatus() : "planned")
                .budget(request.getBudget())
                .currencyId(request.getCurrencyId())
                .locationId(request.getLocationId())
                .acceptRedirect(request.getAcceptRedirect())
                .declineRedirect(request.getDeclineRedirect())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        return toEventDto(eventRepository.save(event));
    }

    public FPEventDto updateEvent(UUID id, FPEventCreateRequest request) {
        FPEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getDateStart() != null) event.setDateStart(request.getDateStart());
        if (request.getDateEnd() != null) event.setDateEnd(request.getDateEnd());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        if (request.getBudget() != null) event.setBudget(request.getBudget());
        if (request.getLocationId() != null) event.setLocationId(request.getLocationId());
        return toEventDto(eventRepository.save(event));
    }

    public void deleteEvent(UUID id) {
        FPEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
        event.setDeleted(true);
        eventRepository.save(event);
    }

    public List<FPEventDto> getUpcomingEvents() {
        return eventRepository.findByDateRange(LocalDateTime.now(), LocalDateTime.now().plusMonths(3))
                .stream().map(this::toEventDto).collect(Collectors.toList());
    }

    // Registrations
    public List<EventRegistrationDto> getEventRegistrations(UUID eventId) {
        return registrationRepository.findByEventIdAndDeletedFalse(eventId)
                .stream().map(this::toRegistrationDto).collect(Collectors.toList());
    }

    public EventRegistrationDto registerForEvent(UUID eventId, EventRegistrationDto request) {
        EventRegistration reg = EventRegistration.builder()
                .eventId(eventId)
                .contactId(request.getContactId())
                .leadId(request.getLeadId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .company(request.getCompany())
                .notes(request.getNotes())
                .build();
        return toRegistrationDto(registrationRepository.save(reg));
    }

    // Locations
    public List<FPEventLocationDto> listLocations() {
        return locationRepository.findByDeletedFalse().stream()
                .map(this::toLocationDto).collect(Collectors.toList());
    }

    public FPEventLocationDto createLocation(FPEventLocationDto request, UUID userId) {
        FPEventLocation loc = FPEventLocation.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .capacity(request.getCapacity())
                .createdBy(userId)
                .build();
        return toLocationDto(locationRepository.save(loc));
    }

    // Invitees
    public List<EventInvitee> getEventInvitees(UUID eventId) {
        return inviteeRepository.findByEventIdAndDeletedFalse(eventId);
    }

    private FPEventDto toEventDto(FPEvent event) {
        return FPEventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .dateStart(event.getDateStart())
                .dateEnd(event.getDateEnd())
                .durationHours(event.getDurationHours())
                .durationMinutes(event.getDurationMinutes())
                .status(event.getStatus())
                .budget(event.getBudget())
                .locationId(event.getLocationId())
                .assignedUserId(event.getAssignedUserId())
                .dateEntered(event.getDateEntered())
                .dateModified(event.getDateModified())
                .build();
    }

    private EventRegistrationDto toRegistrationDto(EventRegistration reg) {
        return EventRegistrationDto.builder()
                .id(reg.getId())
                .eventId(reg.getEventId())
                .contactId(reg.getContactId())
                .leadId(reg.getLeadId())
                .firstName(reg.getFirstName())
                .lastName(reg.getLastName())
                .email(reg.getEmail())
                .phone(reg.getPhone())
                .company(reg.getCompany())
                .status(reg.getStatus())
                .acceptStatus(reg.getAcceptStatus())
                .registrationDate(reg.getRegistrationDate())
                .notes(reg.getNotes())
                .build();
    }

    private FPEventLocationDto toLocationDto(FPEventLocation loc) {
        return FPEventLocationDto.builder()
                .id(loc.getId())
                .name(loc.getName())
                .description(loc.getDescription())
                .address(loc.getAddress())
                .city(loc.getCity())
                .state(loc.getState())
                .country(loc.getCountry())
                .postalCode(loc.getPostalCode())
                .capacity(loc.getCapacity())
                .build();
    }
}
