package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.CaseUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseUpdateRepository extends JpaRepository<CaseUpdate, UUID> {
    List<CaseUpdate> findByCaseIdAndDeletedFalseOrderByDateEnteredDesc(UUID caseId);
    Page<CaseUpdate> findByCaseIdAndDeletedFalse(UUID caseId, Pageable pageable);
    List<CaseUpdate> findByCaseIdAndInternalFalseAndDeletedFalseOrderByDateEnteredDesc(UUID caseId);
}
