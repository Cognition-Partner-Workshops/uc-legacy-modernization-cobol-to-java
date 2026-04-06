package com.suitecrm.survey.controller;

import com.suitecrm.survey.dto.*;
import com.suitecrm.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SurveyDto>> listSurveys(Pageable pageable) {
        return ResponseEntity.ok(surveyService.listSurveys(pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SurveyDto>> listByStatus(@PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(surveyService.listSurveysByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SurveyDto> getSurvey(@PathVariable UUID id) {
        return ResponseEntity.ok(surveyService.getSurvey(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SurveyDto> createSurvey(
            @Valid @RequestBody SurveyCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.createSurvey(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SurveyDto> updateSurvey(
            @PathVariable UUID id, @Valid @RequestBody SurveyCreateRequest request) {
        return ResponseEntity.ok(surveyService.updateSurvey(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSurvey(@PathVariable UUID id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SurveyDto>> search(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(surveyService.searchSurveys(q, pageable));
    }

    // Questions
    @GetMapping("/{surveyId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SurveyQuestionDto>> getQuestions(@PathVariable UUID surveyId) {
        return ResponseEntity.ok(surveyService.getQuestions(surveyId));
    }

    @PostMapping("/{surveyId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SurveyQuestionDto> addQuestion(
            @PathVariable UUID surveyId, @RequestBody SurveyQuestionDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.addQuestion(surveyId, request));
    }

    // Responses
    @PostMapping("/{surveyId}/responses")
    public ResponseEntity<SurveyResponseDto> submitResponse(
            @PathVariable UUID surveyId, @RequestBody SurveyResponseDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.submitResponse(surveyId, request));
    }

    @GetMapping("/{surveyId}/responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<SurveyResponseDto>> getResponses(@PathVariable UUID surveyId, Pageable pageable) {
        return ResponseEntity.ok(surveyService.getResponses(surveyId, pageable));
    }

    @GetMapping("/{surveyId}/analytics/happiness")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Double> getAverageHappiness(@PathVariable UUID surveyId) {
        return ResponseEntity.ok(surveyService.getAverageHappiness(surveyId));
    }
}
