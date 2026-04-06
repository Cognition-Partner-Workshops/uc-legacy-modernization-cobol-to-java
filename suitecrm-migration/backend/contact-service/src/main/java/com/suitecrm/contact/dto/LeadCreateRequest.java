package com.suitecrm.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCreateRequest {

    @Size(max = 25)
    private String salutation;

    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String title;

    @Size(max = 100)
    private String department;

    @Size(max = 255)
    private String company;

    @Size(max = 50)
    private String phoneWork;

    @Size(max = 50)
    private String phoneMobile;

    @Size(max = 50)
    private String phoneHome;

    @Size(max = 50)
    private String phoneFax;

    @Size(max = 255)
    private String email;

    @Size(max = 50)
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
    private UUID assignedUserId;
    private UUID campaignId;
}
