package com.suitecrm.connector.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthKeyDto {
    private UUID id;
    private String name;
    private UUID connectorId;
    private String oauthType;
    private String tokenUrl;
    private String authorizeUrl;
    private String scope;
}
