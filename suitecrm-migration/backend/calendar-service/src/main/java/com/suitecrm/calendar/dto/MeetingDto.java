package com.suitecrm.calendar.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeetingDto {
    private UUID id;
    private String name;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private String status;
    private String type;
    private String location;
    private String description;
    private Integer durationHours;
    private Integer durationMinutes;
    private String parentType;
    private UUID parentId;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
