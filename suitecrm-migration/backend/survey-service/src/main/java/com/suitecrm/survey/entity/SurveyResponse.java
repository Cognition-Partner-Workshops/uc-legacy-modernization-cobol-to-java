package com.suitecrm.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_responses", schema = "survey_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "survey_id", nullable = false)
    private UUID surveyId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "happiness")
    private Integer happiness;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (status == null) status = "completed";
    }
}
