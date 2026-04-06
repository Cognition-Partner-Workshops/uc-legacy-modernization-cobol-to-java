package com.suitecrm.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPEventCreateRequest {
    @NotBlank(message = "Event name is required")
    private String name;
    private String description;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Integer durationHours;
    private Integer durationMinutes;
    private String status;
    private BigDecimal budget;
    private UUID currencyId;
    private UUID locationId;
    private String acceptRedirect;
    private String declineRedirect;
}
