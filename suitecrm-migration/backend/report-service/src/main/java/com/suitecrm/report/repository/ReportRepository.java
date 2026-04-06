package com.suitecrm.report.repository;

import com.suitecrm.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findByIdAndDeletedFalse(UUID id);
    Page<Report> findByDeletedFalse(Pageable pageable);
    Page<Report> findByReportTypeAndDeletedFalse(String reportType, Pageable pageable);
    Page<Report> findByModuleNameAndDeletedFalse(String moduleName, Pageable pageable);
    Page<Report> findByFavoriteTrueAndDeletedFalse(Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.deleted = false AND " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.moduleName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Report> searchReports(@Param("search") String search, Pageable pageable);
}
