package com.suitecrm.connector.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccountDto {
    private UUID id;
    private String name;
    private UUID connectorId;
    private UUID userId;
    private String externalId;
    private String application;
    private LocalDateTime dateEntered;
}
