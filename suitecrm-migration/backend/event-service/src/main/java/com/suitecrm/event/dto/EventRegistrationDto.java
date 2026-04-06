package com.suitecrm.event.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationDto {
    private UUID id;
    private UUID eventId;
    private UUID contactId;
    private UUID leadId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String status;
    private String acceptStatus;
    private LocalDateTime registrationDate;
    private String notes;
}
