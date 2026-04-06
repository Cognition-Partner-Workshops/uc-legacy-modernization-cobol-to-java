package com.suitecrm.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CallCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private String direction;
    private String status;
    private String description;
    private Integer durationHours;
    private Integer durationMinutes;
    private String parentType;
    private UUID parentId;
    private Integer reminderTime;
    private UUID assignedUserId;
}
