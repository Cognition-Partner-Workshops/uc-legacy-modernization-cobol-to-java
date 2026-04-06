package com.suitecrm.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactCreateRequest {

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

    @Size(max = 50)
    private String phoneWork;

    @Size(max = 50)
    private String phoneMobile;

    @Size(max = 50)
    private String phoneHome;

    @Size(max = 50)
    private String phoneFax;

    @Size(max = 50)
    private String phoneOther;

    @Size(max = 255)
    private String email;

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
    private UUID reportsToId;
    private String leadSource;
    private LocalDate birthdate;
    private String doNotCall;
    private UUID assignedUserId;
    private UUID campaignId;
}
