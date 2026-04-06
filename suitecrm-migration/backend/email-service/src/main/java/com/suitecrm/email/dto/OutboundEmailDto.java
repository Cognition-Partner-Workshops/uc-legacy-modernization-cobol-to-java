package com.suitecrm.email.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundEmailDto {
    private UUID id;
    private String name;
    private String type;
    private String mailSendType;
    private String mailSmtpType;
    private String mailSmtpServer;
    private Integer mailSmtpPort;
    private String mailSmtpUser;
    private Boolean mailSmtpAuthReq;
    private Boolean mailSmtpSsl;
    private UUID userId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
