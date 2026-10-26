package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import carddemo.records.CodatecnRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DateFormatConverterTest {

    private static CodatecnRecord call(char type, String input, char outType) {
        CodatecnRecord rec = new CodatecnRecord();
        rec.set(CodatecnRecord.TYPE, String.valueOf(type));
        rec.set(CodatecnRecord.INP_DATE, input);
        rec.set(CodatecnRecord.OUTTYPE, String.valueOf(outType));
        DateFormatConverter.convert(rec);
        return rec;
    }

    @Test
    void dashedToCompactLeavesRestOfOutputUntouched() {
        CodatecnRecord rec = call('2', "2024-02-29", '2');
        assertEquals("20240229            ", rec.getText(CodatecnRecord.OUT_DATE));
        assertEquals(" ".repeat(38), rec.getText(CodatecnRecord.ERROR_MSG));
    }

    @Test
    void compactToDashed() {
        assertEquals("2030-12-31          ", call('1', "20301231", '1').getText(CodatecnRecord.OUT_DATE));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "2|20250101|202510",       // not dashed: positional move of bytes 1-4, 6-7, 9-10
        "2|2027/01/01|20270101",
        "2|2023-02-29|20230229",   // impossible date is still moved, as the assembler does
        "1|2023X231|2023-X2-31"
    })
    void invalidDatesFollowAssemblerByteMoves(char type, String input, String expected) {
        char outType = type == '2' ? '2' : '1';
        assertEquals(expected, call(type, input, outType).getText(CodatecnRecord.OUT_DATE).trim());
    }

    @ParameterizedTest
    @CsvSource({"1,2024-01-01,1", "1,20240101,2", "2,2024-01-01,1", "3,20240101,1"})
    void unsupportedCombinationsSetErrorMessage(char type, String input, char outType) {
        CodatecnRecord rec = call(type, input, outType);
        assertEquals("INVALID INPUT", rec.getText(CodatecnRecord.ERROR_MSG).trim());
        assertEquals(" ".repeat(20), rec.getText(CodatecnRecord.OUT_DATE));
    }
}
