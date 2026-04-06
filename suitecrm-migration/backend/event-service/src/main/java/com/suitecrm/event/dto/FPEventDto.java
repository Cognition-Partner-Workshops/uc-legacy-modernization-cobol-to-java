package com.suitecrm.event.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPEventDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Integer durationHours;
    private Integer durationMinutes;
    private String status;
    private BigDecimal budget;
    private UUID locationId;
    private String locationName;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private Long registrationCount;
    private Long acceptedCount;
}
