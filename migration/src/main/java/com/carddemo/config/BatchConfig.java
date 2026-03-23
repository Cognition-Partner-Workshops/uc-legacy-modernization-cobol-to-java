package com.carddemo.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration.
 * Jobs are defined in the batch package and triggered via REST endpoints.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
}
