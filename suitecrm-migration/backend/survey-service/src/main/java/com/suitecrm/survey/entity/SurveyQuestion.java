package com.suitecrm.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_questions", schema = "survey_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestion {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "survey_id", nullable = false)
    private UUID surveyId;

    @Column(name = "question_type", length = 50)
    private String questionType;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "col_count")
    private Integer colCount;

    @Column(name = "max_answers")
    private Integer maxAnswers;

    @Column(name = "randomize_options")
    private Boolean randomizeOptions;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (isRequired == null) isRequired = false;
        if (randomizeOptions == null) randomizeOptions = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
