package com.suitecrm.connector.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorDto {
    private UUID id;
    private String name;
    private String description;
    private String connectorType;
    private String sourceModule;
    private String baseUrl;
    private String apiVersion;
    private String authType;
    private String status;
    private Boolean isEnabled;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
