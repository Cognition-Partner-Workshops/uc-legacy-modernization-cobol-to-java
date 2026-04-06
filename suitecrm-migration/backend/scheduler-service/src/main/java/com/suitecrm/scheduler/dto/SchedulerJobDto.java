package com.suitecrm.scheduler.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SchedulerJobDto {
    private UUID id; private String name; private UUID schedulerId;
    private LocalDateTime executeTime; private String status; private String resolution;
    private String message; private String target;
    private Integer retryCount; private Integer failureCount;
    private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
