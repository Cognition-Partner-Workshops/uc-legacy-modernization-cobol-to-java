package com.suitecrm.notification.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FavoriteDto {
    private UUID id; private String moduleName; private UUID recordId;
    private UUID assignedUserId; private LocalDateTime dateEntered;
}
