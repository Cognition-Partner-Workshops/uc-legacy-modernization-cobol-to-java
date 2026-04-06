package com.suitecrm.template.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PdfTemplateDto {
    private UUID id; private String name; private String type; private String moduleName;
    private String body; private String pdfHeader; private String pdfFooter;
    private String pageSize; private String orientation; private String description;
    private UUID assignedUserId; private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
