package com.suitecrm.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBContentCreateRequest {
    @NotBlank(message = "Title is required")
    private String name;
    private String body;
    private String summary;
    private String status;
    private LocalDateTime activeDate;
    private LocalDateTime expDate;
    private UUID categoryId;
    private List<String> tags;
}
