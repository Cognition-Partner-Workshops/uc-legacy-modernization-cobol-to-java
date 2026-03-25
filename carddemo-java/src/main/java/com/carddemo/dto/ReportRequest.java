package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for report generation - replaces CORPT00C's date range input and report type selection.
 */
public class ReportRequest {

    @NotBlank(message = "Report type is required")
    @Pattern(regexp = "MONTHLY|YEARLY|CUSTOM", message = "Report type must be MONTHLY, YEARLY, or CUSTOM")
    private String reportType;

    private String startDate;

    private String endDate;

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
