package com.suitecrm.kb.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBContentDto {
    private UUID id;
    private String name;
    private String body;
    private String summary;
    private String status;
    private Integer revision;
    private LocalDateTime activeDate;
    private LocalDateTime expDate;
    private Integer viewCount;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private UUID categoryId;
    private String categoryName;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private List<String> tags;
}
