package com.suitecrm.survey.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionDto {
    private UUID id;
    private UUID surveyId;
    private String name;
    private String description;
    private String questionType;
    private Integer sortOrder;
    private Boolean isRequired;
    private Integer rowCount;
    private Integer colCount;
    private Integer maxAnswers;
    private Boolean randomizeOptions;
    private List<SurveyQuestionOptionDto> options;
}
