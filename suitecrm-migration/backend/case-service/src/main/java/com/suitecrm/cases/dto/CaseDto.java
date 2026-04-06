package com.suitecrm.cases.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDto {
    private UUID id;
    private String name;
    private Integer caseNumber;
    private String status;
    private String priority;
    private String type;
    private String description;
    private String resolution;
    private UUID accountId;
    private String accountName;
    private UUID contactId;
    private String contactName;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
