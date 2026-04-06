package com.suitecrm.survey.repository;

import com.suitecrm.survey.entity.SurveyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    Page<SurveyResponse> findBySurveyIdAndDeletedFalse(UUID surveyId, Pageable pageable);

    long countBySurveyIdAndDeletedFalse(UUID surveyId);

    @Query("SELECT AVG(r.happiness) FROM SurveyResponse r WHERE r.surveyId = :surveyId AND r.deleted = false AND r.happiness IS NOT NULL")
    Double getAverageHappiness(@Param("surveyId") UUID surveyId);

    List<SurveyResponse> findByContactIdAndDeletedFalse(UUID contactId);
}
