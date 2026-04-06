package com.suitecrm.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    @NotBlank(message = "Report name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String module;

    @Size(max = 50)
    private String reportType;

    private String description;
    private String content;

    @Size(max = 50)
    private String chartType;

    private Boolean published;
    private UUID assignedUserId;
}
