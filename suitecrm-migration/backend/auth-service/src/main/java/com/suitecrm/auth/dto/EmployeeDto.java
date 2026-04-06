package com.suitecrm.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String title;
    private String department;
    private String phoneWork;
    private String phoneMobile;
    private String email;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    private UUID reportsToId;
    private String reportsToName;
    private String employeeStatus;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
