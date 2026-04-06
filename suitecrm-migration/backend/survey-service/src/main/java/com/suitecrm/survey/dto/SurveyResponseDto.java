package com.suitecrm.survey.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDto {
    private UUID id;
    private UUID surveyId;
    private UUID contactId;
    private UUID accountId;
    private String emailAddress;
    private Integer happiness;
    private String status;
    private LocalDateTime dateEntered;
    private List<SurveyQuestionResponseDto> questionResponses;
}
