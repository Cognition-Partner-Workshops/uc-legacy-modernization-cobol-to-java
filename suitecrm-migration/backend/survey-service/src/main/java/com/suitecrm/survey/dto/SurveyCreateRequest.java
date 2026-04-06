package com.suitecrm.survey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCreateRequest {
    @NotBlank(message = "Survey name is required")
    private String name;
    private String description;
    private String status;
    private String surveyType;
    private String submitText;
    private String satisfiedText;
    private String dissatisfiedText;
    private Boolean isAnonymous;
}
