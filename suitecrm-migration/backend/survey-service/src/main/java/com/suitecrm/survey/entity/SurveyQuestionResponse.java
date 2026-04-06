package com.suitecrm.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_question_responses", schema = "survey_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionResponse {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "survey_response_id", nullable = false)
    private UUID surveyResponseId;

    @Column(name = "survey_question_id", nullable = false)
    private UUID surveyQuestionId;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answer_option_id")
    private UUID answerOptionId;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
    }
}
