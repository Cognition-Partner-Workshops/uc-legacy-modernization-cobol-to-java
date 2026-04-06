package com.suitecrm.contact.mapper;

import com.suitecrm.contact.dto.ContactCreateRequest;
import com.suitecrm.contact.dto.ContactDto;
import com.suitecrm.contact.entity.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public ContactDto toDto(Contact entity) {
        if (entity == null) return null;
        return ContactDto.builder()
                .id(entity.getId())
                .salutation(entity.getSalutation())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .fullName((entity.getFirstName() != null ? entity.getFirstName() + " " : "") + entity.getLastName())
                .title(entity.getTitle())
                .department(entity.getDepartment())
                .phoneWork(entity.getPhoneWork())
                .phoneMobile(entity.getPhoneMobile())
                .phoneHome(entity.getPhoneHome())
                .phoneFax(entity.getPhoneFax())
                .email(entity.getEmail())
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
                .accountId(entity.getAccountId())
                .reportsToId(entity.getReportsToId())
                .leadSource(entity.getLeadSource())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }

    public Contact toEntity(ContactCreateRequest request) {
        if (request == null) return null;
        return Contact.builder()
                .salutation(request.getSalutation())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .department(request.getDepartment())
                .phoneWork(request.getPhoneWork())
                .phoneMobile(request.getPhoneMobile())
                .phoneHome(request.getPhoneHome())
                .phoneFax(request.getPhoneFax())
                .email(request.getEmail())
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
                .accountId(request.getAccountId())
                .reportsToId(request.getReportsToId())
                .leadSource(request.getLeadSource())
                .doNotCall(request.getDoNotCall())
                .assignedUserId(request.getAssignedUserId())
                .campaignId(request.getCampaignId())
                .build();
    }
}
