package com.suitecrm.email.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    private UUID id;
    private String name;
    private String fromAddr;
    private String fromName;
    private String toAddrs;
    private String ccAddrs;
    private String bccAddrs;
    private String replyToAddr;
    private String description;
    private String descriptionHtml;
    private String type;
    private String status;
    private String intent;
    private String messageId;
    private String parentType;
    private UUID parentId;
    private LocalDateTime dateSent;
    private Boolean flagged;
    private UUID mailboxId;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private List<EmailAttachmentDto> attachments;
}
