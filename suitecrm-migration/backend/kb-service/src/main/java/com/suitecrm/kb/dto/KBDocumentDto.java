package com.suitecrm.kb.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBDocumentDto {
    private UUID id;
    private String name;
    private String description;
    private String documentType;
    private String filename;
    private String fileMimeType;
    private String fileUrl;
    private UUID kbContentId;
    private UUID categoryId;
    private String status;
    private LocalDateTime dateEntered;
}
