package com.suitecrm.survey.repository;

import com.suitecrm.survey.entity.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    Page<Survey> findByDeletedFalse(Pageable pageable);

    Page<Survey> findByStatusAndDeletedFalse(String status, Pageable pageable);

    List<Survey> findByAssignedUserIdAndDeletedFalse(UUID userId);

    @Query("SELECT s FROM Survey s WHERE s.deleted = false AND LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Survey> search(@Param("query") String query, Pageable pageable);
}
