package com.suitecrm.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCreateRequest {

    @NotBlank(message = "Document name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String documentType;

    @Size(max = 50)
    private String status;

    private String categoryId;
    private String subcategoryId;
    private String template;
    private String description;
    private LocalDate activeDate;
    private LocalDate expirationDate;
    private UUID relatedDocId;
    private UUID assignedUserId;
}
