package com.suitecrm.targetlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetListCreateRequest {
    @NotBlank(message = "List name is required")
    private String name;
    private String description;
    private String listType;
    private String domainName;
}
