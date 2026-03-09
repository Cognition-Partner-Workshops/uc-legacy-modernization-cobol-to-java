package com.carddemo.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration replacing JCL job scheduling infrastructure.
 *
 * <p>Original JCL jobs used:
 * - Control-M (ECMNTHLY.CTL) for monthly scheduling
 * - CA7 (ECMNTHLY.CA7) for alternative scheduling
 * - Individual JCL procedures (POSTTRAN, INTCALC, CREASTMT, TRANREPT)
 *
 * <p>Spring Batch provides:
 * - Job repository for tracking job executions
 * - Step-based processing with chunk-oriented tasklets
 * - Restart and skip capabilities
 * - Built-in transaction management
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    // Spring Batch auto-configures a JobRepository, JobLauncher, and StepBuilderFactory
    // using the application datasource. Individual job beans are defined in their
    // respective configuration classes.
}
