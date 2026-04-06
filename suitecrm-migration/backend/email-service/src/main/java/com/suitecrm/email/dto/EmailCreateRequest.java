package com.suitecrm.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCreateRequest {
    @NotBlank(message = "Subject is required")
    private String name;
    private String fromAddr;
    private String fromName;
    @NotBlank(message = "To address is required")
    private String toAddrs;
    private String ccAddrs;
    private String bccAddrs;
    private String replyToAddr;
    private String description;
    private String descriptionHtml;
    private String type;
    private String status;
    private String intent;
    private String parentType;
    private UUID parentId;
    private UUID mailboxId;
}
