package com.suitecrm.report.engine;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ReportResult {
    private UUID reportId;
    private String reportName;
    private String reportType;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int totalRows;
}
