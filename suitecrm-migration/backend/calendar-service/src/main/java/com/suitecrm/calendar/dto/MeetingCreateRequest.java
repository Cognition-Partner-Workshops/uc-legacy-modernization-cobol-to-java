package com.suitecrm.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeetingCreateRequest {
    @NotBlank(message = "Name is required")
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
    private Integer reminderTime;
    private UUID assignedUserId;
}
