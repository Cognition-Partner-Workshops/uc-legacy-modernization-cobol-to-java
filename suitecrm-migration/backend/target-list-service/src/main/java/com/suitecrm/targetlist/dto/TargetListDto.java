package com.suitecrm.targetlist.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetListDto {
    private UUID id;
    private String name;
    private String description;
    private String listType;
    private String domainName;
    private Integer entryCount;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
