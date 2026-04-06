package com.suitecrm.survey.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionOptionDto {
    private UUID id;
    private UUID questionId;
    private String name;
    private Integer sortOrder;
}
