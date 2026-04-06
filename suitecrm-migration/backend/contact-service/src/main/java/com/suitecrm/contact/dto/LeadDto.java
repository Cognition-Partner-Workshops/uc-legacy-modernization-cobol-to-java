package com.suitecrm.contact.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto {
    private UUID id;
    private String salutation;
    private String firstName;
    private String lastName;
    private String fullName;
    private String title;
    private String department;
    private String company;
    private String phoneWork;
    private String phoneMobile;
    private String phoneHome;
    private String phoneFax;
    private String email;
    private String status;
    private String statusDescription;
    private String leadSource;
    private String leadSourceDescription;
    private BigDecimal opportunityAmount;
    private String referedBy;
    private String website;
    private String industry;
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
    private Boolean converted;
    private UUID convertedContactId;
    private UUID convertedAccountId;
    private UUID convertedOpportunityId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID campaignId;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
