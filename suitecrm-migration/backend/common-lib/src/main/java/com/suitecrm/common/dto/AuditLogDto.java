package com.suitecrm.common.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private UUID id;
    private UUID entityId;
    private String entityType;
    private String fieldName;
    private String beforeValue;
    private String afterValue;
    private UUID changedBy;
    private LocalDateTime changedAt;
}
