package com.suitecrm.targetlist.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String title;
    private String department;
    private String phoneWork;
    private String phoneMobile;
    private String emailAddress;
    private String primaryAddressCity;
    private String primaryAddressState;
    private String primaryAddressCountry;
    private String accountName;
    private Boolean doNotCall;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
}
