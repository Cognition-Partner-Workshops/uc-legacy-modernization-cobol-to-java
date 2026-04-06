package com.suitecrm.survey.repository;

import com.suitecrm.survey.entity.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, UUID> {

    List<SurveyQuestionOption> findByQuestionIdAndDeletedFalseOrderBySortOrderAsc(UUID questionId);
}
