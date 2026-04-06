package com.suitecrm.report.repository;

import com.suitecrm.report.entity.ReportField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportFieldRepository extends JpaRepository<ReportField, UUID> {
    List<ReportField> findByReportIdAndDeletedFalseOrderByFieldOrderAsc(UUID reportId);
}
