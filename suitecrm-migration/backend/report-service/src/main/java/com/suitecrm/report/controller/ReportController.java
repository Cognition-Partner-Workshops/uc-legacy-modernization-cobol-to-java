package com.suitecrm.report.controller;

import com.suitecrm.report.entity.Report;
import com.suitecrm.report.repository.ReportRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Report>> listReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String reportType) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Report> reports;
        if (search != null && !search.isBlank()) {
            reports = reportRepository.searchReports(search, pageable);
        } else if (reportType != null && !reportType.isBlank()) {
            reports = reportRepository.findByReportTypeAndDeletedFalse(reportType, pageable);
        } else {
            reports = reportRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Report> getReport(@PathVariable UUID id) {
        return ResponseEntity.ok(reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found")));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report report) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportRepository.save(report));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Report> updateReport(@PathVariable UUID id, @Valid @RequestBody Report update) {
        Report existing = reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        existing.setName(update.getName());
        existing.setReportType(update.getReportType());
        existing.setModuleName(update.getModuleName());
        existing.setContent(update.getContent());
        existing.setChartType(update.getChartType());
        existing.setDescription(update.getDescription());
        return ResponseEntity.ok(reportRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID id) {
        Report report = reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setDeleted(true);
        reportRepository.save(report);
        return ResponseEntity.noContent().build();
    }
}
