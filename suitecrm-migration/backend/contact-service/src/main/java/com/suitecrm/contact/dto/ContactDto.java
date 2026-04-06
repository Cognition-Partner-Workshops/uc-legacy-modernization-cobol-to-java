package com.suitecrm.contact.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {
    private UUID id;
    private String salutation;
    private String firstName;
    private String lastName;
    private String fullName;
    private String title;
    private String department;
    private String phoneWork;
    private String phoneMobile;
    private String phoneHome;
    private String phoneFax;
    private String phoneOther;
    private String email;
    private String emailOptOut;
    private String primaryAddressStreet;
    private String primaryAddressCity;
    private String primaryAddressState;
    private String primaryAddressPostalcode;
    private String primaryAddressCountry;
    private String altAddressStreet;
    private String altAddressCity;
    private String altAddressState;
    private String altAddressPostalcode;
    private String altAddressCountry;
    private String description;
    private UUID accountId;
    private String accountName;
    private UUID reportsToId;
    private String reportsToName;
    private String leadSource;
    private LocalDate birthdate;
    private String doNotCall;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID campaignId;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
