package com.carddemo.statement.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "statements", schema = "statement")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Statement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "acct_id", length = 11, nullable = false) private String acctId;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "file_path", length = 500) private String filePath;
    @Column(name = "html_path", length = 500) private String htmlPath;
    @Column(name = "generated_at") private LocalDateTime generatedAt;
}
