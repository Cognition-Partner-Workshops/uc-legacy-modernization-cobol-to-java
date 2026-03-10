package com.carddemo.batch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsutldtcJobTest {

    @Test
    void validateDate_validIsoDate() {
        String result = CsutldtcJob.validateDate("2024-01-15", "YYYY-MM-DD");
        assertThat(result).contains("Date is valid");
    }

    @Test
    void validateDate_invalidDate() {
        String result = CsutldtcJob.validateDate("2024-13-15", "YYYY-MM-DD");
        assertThat(result).contains("Datevalue error");
    }

    @Test
    void validateDate_nullDate() {
        String result = CsutldtcJob.validateDate(null, "YYYY-MM-DD");
        assertThat(result).contains("Insufficient");
    }

    @Test
    void validateDate_emptyDate() {
        String result = CsutldtcJob.validateDate("", "YYYY-MM-DD");
        assertThat(result).contains("Insufficient");
    }

    @Test
    void validateDate_resultIs80Chars() {
        String result = CsutldtcJob.validateDate("2024-01-15", "YYYY-MM-DD");
        assertThat(result.length()).isEqualTo(80);
    }
}
