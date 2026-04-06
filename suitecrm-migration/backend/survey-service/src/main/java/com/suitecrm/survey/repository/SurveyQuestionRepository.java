package com.suitecrm.survey.repository;

import com.suitecrm.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, UUID> {

    List<SurveyQuestion> findBySurveyIdAndDeletedFalseOrderBySortOrderAsc(UUID surveyId);

    long countBySurveyIdAndDeletedFalse(UUID surveyId);
}
