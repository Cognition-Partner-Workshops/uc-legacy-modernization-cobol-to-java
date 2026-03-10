package com.carddemo.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DateConverterTest {

    @Test
    void toCompact_convertsIsoToCompact() {
        assertThat(DateConverter.toCompact("2024-01-15")).isEqualTo("20240115");
        assertThat(DateConverter.toCompact("1999-12-31")).isEqualTo("19991231");
    }

    @Test
    void toCompact_returnsOriginalIfWrongLength() {
        assertThat(DateConverter.toCompact("20240115")).isEqualTo("20240115");
        assertThat(DateConverter.toCompact("")).isEqualTo("");
    }

    @Test
    void toCompact_nullReturnsNull() {
        assertThat(DateConverter.toCompact(null)).isNull();
    }

    @Test
    void toIso_convertsCompactToIso() {
        assertThat(DateConverter.toIso("20240115")).isEqualTo("2024-01-15");
        assertThat(DateConverter.toIso("19991231")).isEqualTo("1999-12-31");
    }

    @Test
    void toIso_returnsOriginalIfWrongLength() {
        assertThat(DateConverter.toIso("2024-01-15")).isEqualTo("2024-01-15");
    }

    @Test
    void toIso_nullReturnsNull() {
        assertThat(DateConverter.toIso(null)).isNull();
    }
}
