package com.suitecrm.activity.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private UUID id;
    private String name;
    private String status;
    private String type;
    private String location;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Integer durationHours;
    private Integer durationMinutes;
    private String description;
    private UUID parentId;
    private String parentType;
    private UUID accountId;
    private UUID contactId;
    private String password;
    private String displayedUrl;
    private String externalId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
