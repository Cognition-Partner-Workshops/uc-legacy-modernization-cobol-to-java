package com.suitecrm.report.repository;

import com.suitecrm.report.entity.ReportCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportConditionRepository extends JpaRepository<ReportCondition, UUID> {
    List<ReportCondition> findByReportIdAndDeletedFalseOrderByOrderNum(UUID reportId);
    void deleteByReportId(UUID reportId);
}
