package com.suitecrm.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreateRequest {

    @NotBlank(message = "Case subject is required")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String status;

    @Size(max = 50)
    private String priority;

    @Size(max = 100)
    private String type;

    private String description;
    private String resolution;
    private UUID accountId;
    private UUID contactId;
    private UUID assignedUserId;
}
