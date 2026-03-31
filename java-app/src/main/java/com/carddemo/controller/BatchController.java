package com.carddemo.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job transactionPostingJob;
    private final Job interestCalculationJob;
    private final Job statementGenerationJob;

    public BatchController(JobLauncher jobLauncher,
                           @Qualifier("transactionPostingJob") Job transactionPostingJob,
                           @Qualifier("interestCalculationJob") Job interestCalculationJob,
                           @Qualifier("statementGenerationJob") Job statementGenerationJob) {
        this.jobLauncher = jobLauncher;
        this.transactionPostingJob = transactionPostingJob;
        this.interestCalculationJob = interestCalculationJob;
        this.statementGenerationJob = statementGenerationJob;
    }

    @PostMapping("/post-transactions")
    public ResponseEntity<Map<String, String>> postTransactions() {
        return launchJob(transactionPostingJob, "Transaction Posting");
    }

    @PostMapping("/calculate-interest")
    public ResponseEntity<Map<String, String>> calculateInterest() {
        return launchJob(interestCalculationJob, "Interest Calculation");
    }

    @PostMapping("/generate-statements")
    public ResponseEntity<Map<String, String>> generateStatements() {
        return launchJob(statementGenerationJob, "Statement Generation");
    }

    private ResponseEntity<Map<String, String>> launchJob(Job job, String jobName) {
        Map<String, String> response = new HashMap<>();
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            response.put("status", "SUCCESS");
            response.put("message", jobName + " job launched successfully.");
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", jobName + " job failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
