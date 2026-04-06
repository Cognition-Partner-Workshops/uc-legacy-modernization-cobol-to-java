package com.suitecrm.activity.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private UUID id;
    private String name;
    private String status;
    private String priority;
    private LocalDate dateDue;
    private LocalDate dateStart;
    private String description;
    private UUID parentId;
    private String parentType;
    private UUID contactId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
