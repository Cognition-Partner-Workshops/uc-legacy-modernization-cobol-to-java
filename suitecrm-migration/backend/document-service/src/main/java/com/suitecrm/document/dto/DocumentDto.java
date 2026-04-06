package com.suitecrm.document.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private UUID id;
    private String name;
    private String documentType;
    private String status;
    private String categoryId;
    private String subcategoryId;
    private String template;
    private String description;
    private LocalDate activeDate;
    private LocalDate expirationDate;
    private UUID relatedDocId;
    private String relatedDocRevId;
    private String documentRevisionId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
