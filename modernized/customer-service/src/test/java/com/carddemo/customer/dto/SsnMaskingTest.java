package com.carddemo.customer.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SsnMaskingTest {

    @Test
    void maskSsn_shouldMask9DigitSsn() {
        assertThat(CustomerDto.maskSsn("123456789")).isEqualTo("***-**-6789");
    }

    @Test
    void maskSsn_shouldHandleNull() {
        assertThat(CustomerDto.maskSsn(null)).isNull();
    }

    @Test
    void maskSsn_shouldHandleShortString() {
        assertThat(CustomerDto.maskSsn("12")).isEqualTo("12");
    }

    @Test
    void maskSsn_shouldShowLast4Digits() {
        assertThat(CustomerDto.maskSsn("987654321")).isEqualTo("***-**-4321");
    }
}
