package com.suitecrm.survey.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDto {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String surveyType;
    private String submitText;
    private String satisfiedText;
    private String dissatisfiedText;
    private Boolean isAnonymous;
    private Integer responseCount;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private List<SurveyQuestionDto> questions;
}
