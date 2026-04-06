package com.suitecrm.report.service;

import com.suitecrm.report.dto.*;
import com.suitecrm.report.entity.*;
import com.suitecrm.report.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportConditionRepository reportConditionRepository;
    private final ReportFieldRepository reportFieldRepository;
    private final ScheduledReportRepository scheduledReportRepository;

    @Transactional(readOnly = true)
    public Page<ReportDto> listReports(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Report> reports;
        if (search != null && !search.isBlank()) {
            reports = reportRepository.searchReports(search, pageable);
        } else {
            reports = reportRepository.findByDeletedFalse(pageable);
        }
        return reports.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ReportDto getReport(UUID id) {
        Report report = reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return toDto(report);
    }

    public ReportDto createReport(ReportCreateRequest request, UUID createdBy) {
        log.info("Creating report: name={}", request.getName());
        Report report = Report.builder()
                .name(request.getName())
                .module(request.getModule())
                .reportType(request.getReportType())
                .description(request.getDescription())
                .content(request.getContent())
                .chartType(request.getChartType())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Report saved = reportRepository.save(report);
        log.info("Created report: id={}", saved.getId());
        return toDto(saved);
    }

    public ReportDto updateReport(UUID id, ReportCreateRequest request) {
        Report existing = reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        existing.setName(request.getName());
        existing.setModule(request.getModule());
        existing.setReportType(request.getReportType());
        existing.setDescription(request.getDescription());
        existing.setContent(request.getContent());
        existing.setChartType(request.getChartType());
        existing.setPublished(request.getPublished());
        existing.setAssignedUserId(request.getAssignedUserId());
        Report saved = reportRepository.save(existing);
        return toDto(saved);
    }

    public void deleteReport(UUID id) {
        Report report = reportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        report.setDeleted(true);
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportCondition> getReportConditions(UUID reportId) {
        return reportConditionRepository.findByReportIdAndDeletedFalseOrderByOrderNum(reportId);
    }

    @Transactional(readOnly = true)
    public List<ReportField> getReportFields(UUID reportId) {
        return reportFieldRepository.findByReportIdAndDeletedFalseOrderByOrderNum(reportId);
    }

    @Transactional(readOnly = true)
    public List<ScheduledReport> getScheduledReports(UUID reportId) {
        return scheduledReportRepository.findByReportIdAndDeletedFalse(reportId);
    }

    public ScheduledReport scheduleReport(ScheduledReport scheduledReport) {
        return scheduledReportRepository.save(scheduledReport);
    }

    private ReportDto toDto(Report entity) {
        return ReportDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .module(entity.getModule())
                .reportType(entity.getReportType())
                .description(entity.getDescription())
                .content(entity.getContent())
                .chartType(entity.getChartType())
                .published(entity.getPublished())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
