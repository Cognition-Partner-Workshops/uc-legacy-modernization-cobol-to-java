package com.suitecrm.common.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {
    private UUID eventId;
    private String eventType;
    private String entityType;
    private UUID entityId;
    private String payload;
    private UUID triggeredBy;
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    public static DomainEvent of(String eventType, String entityType, UUID entityId, String payload, UUID triggeredBy) {
        return DomainEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .payload(payload)
                .triggeredBy(triggeredBy)
                .build();
    }
}
