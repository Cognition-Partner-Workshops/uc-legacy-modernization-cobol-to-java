package com.suitecrm.report.repository;

import com.suitecrm.report.entity.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduledReportRepository extends JpaRepository<ScheduledReport, UUID> {
    Optional<ScheduledReport> findByIdAndDeletedFalse(UUID id);
    List<ScheduledReport> findByReportIdAndDeletedFalse(UUID reportId);
    List<ScheduledReport> findByActiveTrueAndDeletedFalse();

    @Query("SELECT sr FROM ScheduledReport sr WHERE sr.active = true AND sr.deleted = false AND sr.nextRun <= :now")
    List<ScheduledReport> findDueReports(@Param("now") LocalDateTime now);
}
