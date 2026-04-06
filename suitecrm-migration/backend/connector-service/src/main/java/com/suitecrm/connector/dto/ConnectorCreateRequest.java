package com.suitecrm.connector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorCreateRequest {
    @NotBlank(message = "Connector name is required")
    private String name;
    private String description;
    private String connectorType;
    private String sourceModule;
    private String connectorClass;
    private String baseUrl;
    private String apiVersion;
    private String authType;
    private String configJson;
    private String fieldMapping;
}
