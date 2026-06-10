package com.carddemo.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final JobLauncher jobLauncher;
    private final Job postTransactionJob;
    private final Job interestCalculationJob;
    private final Job statementGenerationJob;

    public AdminController(JobLauncher jobLauncher,
                           @Qualifier("postTransactionJob") Job postTransactionJob,
                           @Qualifier("interestCalculationJob") Job interestCalculationJob,
                           @Qualifier("statementGenerationJob") Job statementGenerationJob) {
        this.jobLauncher = jobLauncher;
        this.postTransactionJob = postTransactionJob;
        this.interestCalculationJob = interestCalculationJob;
        this.statementGenerationJob = statementGenerationJob;
    }

    @PostMapping("/jobs/post-transactions")
    public ResponseEntity<Map<String, String>> runPostTransactions() throws Exception {
        return launchJob(postTransactionJob, "postTransactionJob");
    }

    @PostMapping("/jobs/interest-calculation")
    public ResponseEntity<Map<String, String>> runInterestCalculation() throws Exception {
        return launchJob(interestCalculationJob, "interestCalculationJob");
    }

    @PostMapping("/jobs/statement-generation")
    public ResponseEntity<Map<String, String>> runStatementGeneration() throws Exception {
        return launchJob(statementGenerationJob, "statementGenerationJob");
    }

    private ResponseEntity<Map<String, String>> launchJob(Job job, String jobName) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(job, params);
        String executionId = String.valueOf(execution.getId());
        String status = execution.getStatus().name();
        if (execution.getStatus() == BatchStatus.FAILED) {
            return ResponseEntity.internalServerError().body(
                    Map.of("status", status, "job", jobName, "executionId", executionId));
        }
        return ResponseEntity.ok(Map.of("status", status, "job", jobName, "executionId", executionId));
    }
}
