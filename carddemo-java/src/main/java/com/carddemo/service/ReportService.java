package com.carddemo.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    private final JobLauncher jobLauncher;
    private final Job transactionReportJob;

    public ReportService(JobLauncher jobLauncher,
                         @Qualifier("transactionReportJob") Job transactionReportJob) {
        this.jobLauncher = jobLauncher;
        this.transactionReportJob = transactionReportJob;
    }

    /**
     * Request report generation - mirrors CORPT00C.cbl
     * Triggers batch report generation with specified type and date range
     */
    public void requestReport(String reportType, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("reportType", reportType)
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(transactionReportJob, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch report job: " + e.getMessage(), e);
        }
    }
}
