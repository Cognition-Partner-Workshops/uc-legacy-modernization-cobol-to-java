package com.suitecrm.activity.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallDto {
    private UUID id;
    private String name;
    private String direction;
    private String status;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Integer durationHours;
    private Integer durationMinutes;
    private String description;
    private UUID parentId;
    private String parentType;
    private UUID accountId;
    private UUID contactId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
