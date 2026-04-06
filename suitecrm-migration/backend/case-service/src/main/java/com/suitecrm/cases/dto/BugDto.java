package com.suitecrm.cases.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugDto {
    private UUID id;
    private String name;
    private Integer bugNumber;
    private String status;
    private String priority;
    private String type;
    private String source;
    private String resolution;
    private String fixedInRelease;
    private String foundInRelease;
    private String productCategory;
    private String description;
    private String workLog;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
