package com.suitecrm.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityGroupDto {

    private UUID id;
    private String name;
    private String description;
    private Boolean noninheritable;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
