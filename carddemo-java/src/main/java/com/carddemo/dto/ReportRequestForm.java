package com.carddemo.dto;

public class ReportRequestForm {
    private String reportType;
    private String startDate;
    private String endDate;

    public ReportRequestForm() {}

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
