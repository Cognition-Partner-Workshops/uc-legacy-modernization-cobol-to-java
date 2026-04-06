package com.suitecrm.report.engine;

import com.suitecrm.report.entity.Report;
import com.suitecrm.report.entity.ReportCondition;
import com.suitecrm.report.entity.ReportField;
import com.suitecrm.report.repository.ReportConditionRepository;
import com.suitecrm.report.repository.ReportFieldRepository;
import com.suitecrm.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportEngine {

    private final ReportRepository reportRepository;
    private final ReportFieldRepository fieldRepository;
    private final ReportConditionRepository conditionRepository;
    private final SqlQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;

    public ReportResult generateReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        List<ReportField> fields = fieldRepository.findByReportIdAndDeletedFalseOrderByFieldOrderAsc(reportId);
        List<ReportCondition> conditions = conditionRepository.findByReportIdAndDeletedFalseOrderByConditionOrderAsc(reportId);

        String sql = queryBuilder.buildQuery(report, fields, conditions);
        log.info("Executing report query: {}", sql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        ReportResult result = new ReportResult();
        result.setReportId(reportId);
        result.setReportName(report.getName());
        result.setReportType(report.getReportType());
        result.setColumns(fields.stream().map(f -> f.getLabel() != null ? f.getLabel() : f.getField()).toList());
        result.setRows(rows);
        result.setTotalRows(rows.size());
        return result;
    }
}
