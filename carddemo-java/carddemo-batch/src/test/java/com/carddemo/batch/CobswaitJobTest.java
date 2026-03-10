package com.carddemo.batch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CobswaitJobTest {

    @Test
    void wait_completesWithoutError() {
        long start = System.currentTimeMillis();
        CobswaitJob.wait(10); // 10 centiseconds = 100ms
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isGreaterThanOrEqualTo(80); // Allow some tolerance
    }

    @Test
    void wait_zeroDoesNotBlock() {
        long start = System.currentTimeMillis();
        CobswaitJob.wait(0);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(100);
    }
}
