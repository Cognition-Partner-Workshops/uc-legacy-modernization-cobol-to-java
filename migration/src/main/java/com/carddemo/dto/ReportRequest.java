package com.carddemo.dto;

import java.time.LocalDate;

public class ReportRequest {

    private String acctId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType;

    public ReportRequest() {}

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
}
