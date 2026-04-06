package com.suitecrm.email.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundEmailDto {
    private UUID id;
    private String name;
    private String status;
    private String serverUrl;
    private String emailUser;
    private String port;
    private String protocol;
    private String mailboxType;
    private String service;
    private Boolean isPersonal;
    private Boolean isSsl;
    private Boolean deleteSeen;
    private String mailbox;
    private UUID groupId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
