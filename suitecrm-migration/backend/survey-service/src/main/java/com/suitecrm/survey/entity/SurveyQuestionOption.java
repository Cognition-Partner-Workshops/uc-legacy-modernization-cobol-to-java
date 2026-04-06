package com.suitecrm.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_question_options", schema = "survey_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionOption {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
    }
}
