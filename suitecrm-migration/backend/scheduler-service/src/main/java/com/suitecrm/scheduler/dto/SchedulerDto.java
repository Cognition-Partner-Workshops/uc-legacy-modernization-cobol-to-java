package com.suitecrm.scheduler.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SchedulerDto {
    private UUID id; private String name; private String job; private String jobInterval;
    private String timeFrom; private String timeTo; private LocalDateTime lastRun;
    private String status; private Boolean catchUp;
    private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
