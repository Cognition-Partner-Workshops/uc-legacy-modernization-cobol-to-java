package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.CaseEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseEventRepository extends JpaRepository<CaseEvent, UUID> {
    List<CaseEvent> findByCaseIdAndDeletedFalseOrderByDateEnteredDesc(UUID caseId);
    Page<CaseEvent> findByCaseIdAndDeletedFalse(UUID caseId, Pageable pageable);
}
