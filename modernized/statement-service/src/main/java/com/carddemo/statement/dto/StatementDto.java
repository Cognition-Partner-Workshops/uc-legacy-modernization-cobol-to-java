package com.carddemo.statement.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatementDto {
    private Long id;
    private String acctId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String filePath;
    private String htmlPath;
    private LocalDateTime generatedAt;
}
