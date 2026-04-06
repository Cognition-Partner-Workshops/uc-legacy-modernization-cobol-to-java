package com.suitecrm.template.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailTemplateDto {
    private UUID id; private String name; private String subject; private String body; private String bodyHtml;
    private String type; private Boolean textOnly; private String description;
    private UUID assignedUserId; private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
