package com.carddemo.batch.ebcdic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PackedDecimalDecoderTest {
    @Test
    void decodesPositiveCreditSign() {
        assertThat(PackedDecimalDecoder.decode(new byte[] {(byte) 0x12, (byte) 0x3C}, 2))
                .isEqualByComparingTo(new BigDecimal("1.23"));
    }

    @Test
    void decodesNegativeDebitSign() {
        assertThat(PackedDecimalDecoder.decode(new byte[] {(byte) 0x12, (byte) 0x3D}, 2))
                .isEqualByComparingTo(new BigDecimal("-1.23"));
    }

    @Test
    void acceptsUnsignedPositiveSign() {
        assertThat(PackedDecimalDecoder.decode(new byte[] {(byte) 0x12, (byte) 0x3F}, 0))
                .isEqualByComparingTo(new BigDecimal("123"));
    }

    @Test
    void rejectsInvalidSign() {
        assertThatThrownBy(() -> PackedDecimalDecoder.decode(new byte[] {(byte) 0x12, (byte) 0x3A}, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
