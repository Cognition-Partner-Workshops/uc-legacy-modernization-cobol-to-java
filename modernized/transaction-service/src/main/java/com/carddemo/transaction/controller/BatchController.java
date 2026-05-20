package com.carddemo.transaction.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job transactionPostingJob;

    public BatchController(JobLauncher jobLauncher, Job transactionPostingJob) {
        this.jobLauncher = jobLauncher;
        this.transactionPostingJob = transactionPostingJob;
    }

    @PostMapping("/post-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> postTransactions() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(transactionPostingJob, params);
        return ResponseEntity.ok(Map.of("status", "STARTED", "job", "transactionPostingJob"));
    }
}
