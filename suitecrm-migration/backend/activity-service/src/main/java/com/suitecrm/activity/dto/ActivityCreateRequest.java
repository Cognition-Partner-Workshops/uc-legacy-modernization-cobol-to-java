package com.suitecrm.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String status;

    @Size(max = 50)
    private String priority;

    @Size(max = 50)
    private String direction;

    @Size(max = 50)
    private String type;

    @Size(max = 255)
    private String location;

    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private LocalDate dateDue;
    private Integer durationHours;
    private Integer durationMinutes;
    private String description;
    private UUID parentId;
    private String parentType;
    private UUID contactId;
    private UUID accountId;
    private UUID assignedUserId;
}
