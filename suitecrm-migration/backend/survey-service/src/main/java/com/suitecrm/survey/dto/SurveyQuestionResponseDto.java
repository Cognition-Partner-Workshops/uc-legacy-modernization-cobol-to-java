package com.suitecrm.survey.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionResponseDto {
    private UUID id;
    private UUID surveyQuestionId;
    private String answer;
    private UUID answerOptionId;
}
