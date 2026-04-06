package com.suitecrm.notification.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlertDto {
    private UUID id; private String name; private String alertType; private String urlRedirect;
    private String targetModule; private String description; private Boolean isRead;
    private UUID assignedUserId; private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
