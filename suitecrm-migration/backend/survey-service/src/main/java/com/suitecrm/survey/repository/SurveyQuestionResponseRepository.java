package com.suitecrm.survey.repository;

import com.suitecrm.survey.entity.SurveyQuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyQuestionResponseRepository extends JpaRepository<SurveyQuestionResponse, UUID> {

    List<SurveyQuestionResponse> findBySurveyResponseId(UUID surveyResponseId);

    List<SurveyQuestionResponse> findBySurveyQuestionId(UUID questionId);

    @Query("SELECT qr.answerOptionId, COUNT(qr) FROM SurveyQuestionResponse qr WHERE qr.surveyQuestionId = :questionId AND qr.answerOptionId IS NOT NULL GROUP BY qr.answerOptionId")
    List<Object[]> getOptionCounts(@Param("questionId") UUID questionId);
}
