package com.suitecrm.survey.service;

import com.suitecrm.survey.dto.*;
import com.suitecrm.survey.entity.*;
import com.suitecrm.survey.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyQuestionResponseRepository questionResponseRepository;

    public Page<SurveyDto> listSurveys(Pageable pageable) {
        return surveyRepository.findByDeletedFalse(pageable).map(this::toSurveyDto);
    }

    public Page<SurveyDto> listSurveysByStatus(String status, Pageable pageable) {
        return surveyRepository.findByStatusAndDeletedFalse(status, pageable).map(this::toSurveyDto);
    }

    public SurveyDto getSurvey(UUID id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found: " + id));
        SurveyDto dto = toSurveyDto(survey);
        List<SurveyQuestion> questions = questionRepository.findBySurveyIdAndDeletedFalseOrderBySortOrderAsc(id);
        dto.setQuestions(questions.stream().map(q -> {
            SurveyQuestionDto qDto = toQuestionDto(q);
            qDto.setOptions(optionRepository.findByQuestionIdAndDeletedFalseOrderBySortOrderAsc(q.getId())
                    .stream().map(this::toOptionDto).collect(Collectors.toList()));
            return qDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    public SurveyDto createSurvey(SurveyCreateRequest request, UUID userId) {
        Survey survey = Survey.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "draft")
                .surveyType(request.getSurveyType())
                .submitText(request.getSubmitText())
                .satisfiedText(request.getSatisfiedText())
                .dissatisfiedText(request.getDissatisfiedText())
                .isAnonymous(request.getIsAnonymous())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        return toSurveyDto(surveyRepository.save(survey));
    }

    public SurveyDto updateSurvey(UUID id, SurveyCreateRequest request) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found: " + id));
        if (request.getName() != null) survey.setName(request.getName());
        if (request.getDescription() != null) survey.setDescription(request.getDescription());
        if (request.getStatus() != null) survey.setStatus(request.getStatus());
        if (request.getSurveyType() != null) survey.setSurveyType(request.getSurveyType());
        if (request.getIsAnonymous() != null) survey.setIsAnonymous(request.getIsAnonymous());
        return toSurveyDto(surveyRepository.save(survey));
    }

    public void deleteSurvey(UUID id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found: " + id));
        survey.setDeleted(true);
        surveyRepository.save(survey);
    }

    // Questions
    public SurveyQuestionDto addQuestion(UUID surveyId, SurveyQuestionDto request) {
        SurveyQuestion question = SurveyQuestion.builder()
                .surveyId(surveyId)
                .name(request.getName())
                .description(request.getDescription())
                .questionType(request.getQuestionType())
                .sortOrder(request.getSortOrder())
                .isRequired(request.getIsRequired())
                .rowCount(request.getRowCount())
                .colCount(request.getColCount())
                .maxAnswers(request.getMaxAnswers())
                .randomizeOptions(request.getRandomizeOptions())
                .build();
        return toQuestionDto(questionRepository.save(question));
    }

    public List<SurveyQuestionDto> getQuestions(UUID surveyId) {
        return questionRepository.findBySurveyIdAndDeletedFalseOrderBySortOrderAsc(surveyId)
                .stream().map(q -> {
                    SurveyQuestionDto dto = toQuestionDto(q);
                    dto.setOptions(optionRepository.findByQuestionIdAndDeletedFalseOrderBySortOrderAsc(q.getId())
                            .stream().map(this::toOptionDto).collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());
    }

    // Responses
    public SurveyResponseDto submitResponse(UUID surveyId, SurveyResponseDto request) {
        SurveyResponse response = SurveyResponse.builder()
                .surveyId(surveyId)
                .contactId(request.getContactId())
                .accountId(request.getAccountId())
                .emailAddress(request.getEmailAddress())
                .happiness(request.getHappiness())
                .build();
        SurveyResponse saved = responseRepository.save(response);

        if (request.getQuestionResponses() != null) {
            for (SurveyQuestionResponseDto qr : request.getQuestionResponses()) {
                SurveyQuestionResponse questionResponse = SurveyQuestionResponse.builder()
                        .surveyResponseId(saved.getId())
                        .surveyQuestionId(qr.getSurveyQuestionId())
                        .answer(qr.getAnswer())
                        .answerOptionId(qr.getAnswerOptionId())
                        .build();
                questionResponseRepository.save(questionResponse);
            }
        }

        // Update response count
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found: " + surveyId));
        survey.setResponseCount((int) responseRepository.countBySurveyIdAndDeletedFalse(surveyId));
        surveyRepository.save(survey);

        return toResponseDto(saved);
    }

    public Page<SurveyResponseDto> getResponses(UUID surveyId, Pageable pageable) {
        return responseRepository.findBySurveyIdAndDeletedFalse(surveyId, pageable).map(this::toResponseDto);
    }

    public Double getAverageHappiness(UUID surveyId) {
        return responseRepository.getAverageHappiness(surveyId);
    }

    public Page<SurveyDto> searchSurveys(String query, Pageable pageable) {
        return surveyRepository.search(query, pageable).map(this::toSurveyDto);
    }

    // Mappers
    private SurveyDto toSurveyDto(Survey survey) {
        return SurveyDto.builder()
                .id(survey.getId())
                .name(survey.getName())
                .description(survey.getDescription())
                .status(survey.getStatus())
                .surveyType(survey.getSurveyType())
                .submitText(survey.getSubmitText())
                .satisfiedText(survey.getSatisfiedText())
                .dissatisfiedText(survey.getDissatisfiedText())
                .isAnonymous(survey.getIsAnonymous())
                .responseCount(survey.getResponseCount())
                .assignedUserId(survey.getAssignedUserId())
                .dateEntered(survey.getDateEntered())
                .dateModified(survey.getDateModified())
                .build();
    }

    private SurveyQuestionDto toQuestionDto(SurveyQuestion question) {
        return SurveyQuestionDto.builder()
                .id(question.getId())
                .surveyId(question.getSurveyId())
                .name(question.getName())
                .description(question.getDescription())
                .questionType(question.getQuestionType())
                .sortOrder(question.getSortOrder())
                .isRequired(question.getIsRequired())
                .rowCount(question.getRowCount())
                .colCount(question.getColCount())
                .maxAnswers(question.getMaxAnswers())
                .randomizeOptions(question.getRandomizeOptions())
                .build();
    }

    private SurveyQuestionOptionDto toOptionDto(SurveyQuestionOption option) {
        return SurveyQuestionOptionDto.builder()
                .id(option.getId())
                .questionId(option.getQuestionId())
                .name(option.getName())
                .sortOrder(option.getSortOrder())
                .build();
    }

    private SurveyResponseDto toResponseDto(SurveyResponse response) {
        return SurveyResponseDto.builder()
                .id(response.getId())
                .surveyId(response.getSurveyId())
                .contactId(response.getContactId())
                .accountId(response.getAccountId())
                .emailAddress(response.getEmailAddress())
                .happiness(response.getHappiness())
                .status(response.getStatus())
                .dateEntered(response.getDateEntered())
                .build();
    }
}
