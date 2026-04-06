package com.suitecrm.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "surveys", schema = "survey_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String status;

    @Column(name = "survey_type", length = 50)
    private String surveyType;

    @Column(name = "submit_text")
    private String submitText;

    @Column(name = "satisfied_text")
    private String satisfiedText;

    @Column(name = "dissatisfied_text")
    private String dissatisfiedText;

    @Column(name = "survey_url_parameters")
    private String surveyUrlParameters;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "response_count")
    private Integer responseCount;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

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
        if (responseCount == null) responseCount = 0;
        if (isAnonymous == null) isAnonymous = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
