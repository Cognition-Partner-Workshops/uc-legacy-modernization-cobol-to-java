package com.carddemo.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the copybook-equivalent POJOs (CSDAT01Y, CSUTLDPY, CSUTLDWY). */
class CopybookModelTest {

    @Test
    void csDat01yRendersAllCopybookFormats() {
        CsDat01y dt = CsDat01y.from(LocalDateTime.of(2023, 3, 9, 23, 15, 58, 420_000_000));
        assertEquals(2023, dt.year());
        assertEquals(3, dt.month());
        assertEquals(9, dt.day());
        assertEquals("20230309", dt.curdateN());
        assertEquals("03/09/23", dt.curdateMmDdYy());
        assertEquals("23:15:58", dt.curtimeHhMmSs());
        assertEquals("2023-03-09 23:15:58", dt.timestamp());
        assertEquals(42, dt.milsec());
    }

    @Test
    void csUtlDwyCenturyMonthAndDayPredicates() {
        assertTrue(CsUtlDwy.isValidCentury(20));
        assertTrue(CsUtlDwy.isValidCentury(19));
        assertFalse(CsUtlDwy.isValidCentury(18));

        assertTrue(CsUtlDwy.isValidMonth(1));
        assertTrue(CsUtlDwy.isValidMonth(12));
        assertFalse(CsUtlDwy.isValidMonth(0));
        assertFalse(CsUtlDwy.isValidMonth(13));

        assertTrue(CsUtlDwy.is31DayMonth(1));
        assertTrue(CsUtlDwy.is31DayMonth(12));
        assertFalse(CsUtlDwy.is31DayMonth(2));
        assertFalse(CsUtlDwy.is31DayMonth(4));
    }

    @Test
    void csUtlDwyDefaultsAndAccessors() {
        CsUtlDwy ws = new CsUtlDwy();
        assertEquals("YYYYMMDD", ws.getDateFormat());
        assertEquals(CsUtlDwy.FieldFlag.VALID, ws.getYearFlag());

        ws.setCentury(20);
        ws.setYear(23);
        ws.setMonth(2);
        ws.setDay(29);
        ws.setMonthFlag(CsUtlDwy.FieldFlag.NOT_OK);
        assertEquals(20, ws.getCentury());
        assertEquals(29, ws.getDay());
        assertEquals(CsUtlDwy.FieldFlag.NOT_OK, ws.getMonthFlag());
    }

    @Test
    void csUtlDpyParametersAndSwitch() {
        CsUtlDpy params = new CsUtlDpy("DATE-OF-BIRTH");
        assertEquals("DATE-OF-BIRTH", params.getEditVariableName());
        assertTrue(params.isReturnMsgOff());
        assertFalse(params.isInputError());

        params.setInputError(true);
        params.setReturnMsg("DATE-OF-BIRTH : Year must be supplied.");
        params.setReturnMsgSwitch(CsUtlDpy.ReturnMsgSwitch.ON);
        assertTrue(params.isInputError());
        assertFalse(params.isReturnMsgOff());
        assertEquals("DATE-OF-BIRTH : Year must be supplied.", params.getReturnMsg());
    }
}
