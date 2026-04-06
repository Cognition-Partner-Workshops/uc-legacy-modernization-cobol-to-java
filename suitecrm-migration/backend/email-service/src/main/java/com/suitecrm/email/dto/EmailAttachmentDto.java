package com.suitecrm.email.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentDto {
    private UUID id;
    private UUID emailId;
    private String filename;
    private String fileMimeType;
    private Long fileSize;
    private String fileExt;
    private String storageLocation;
}
