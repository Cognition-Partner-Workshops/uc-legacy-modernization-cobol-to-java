package com.carddemo.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Handles CICS XCTL/RETURN TRANSID logic.
 * Maps COBOL program names to URL paths and transaction IDs.
 */
@Service
public class CicsNavigationService {

    private static final Map<String, String> PROGRAM_TO_URL = Map.ofEntries(
        Map.entry("COSGN00C", "/signon"),
        Map.entry("COMEN01C", "/menu"),
        Map.entry("COADM01C", "/admin"),
        Map.entry("COACTVWC", "/account/view"),
        Map.entry("COACTUPC", "/account/update"),
        Map.entry("COCRDLIC", "/card/list"),
        Map.entry("COCRDSLC", "/card/select"),
        Map.entry("COCRDUPC", "/card/update"),
        Map.entry("COTRN00C", "/transaction/list"),
        Map.entry("COTRN01C", "/transaction/view"),
        Map.entry("COTRN02C", "/transaction/add"),
        Map.entry("CORPT00C", "/report"),
        Map.entry("COBIL00C", "/bill-payment"),
        Map.entry("COUSR00C", "/admin/user/list"),
        Map.entry("COUSR01C", "/admin/user/add"),
        Map.entry("COUSR02C", "/admin/user/update"),
        Map.entry("COUSR03C", "/admin/user/delete")
    );

    private static final Map<String, String> PROGRAM_TO_TRANID = Map.ofEntries(
        Map.entry("COSGN00C", "CC00"),
        Map.entry("COMEN01C", "CM00"),
        Map.entry("COADM01C", "CA00"),
        Map.entry("COACTVWC", "CA01"),
        Map.entry("COACTUPC", "CA02"),
        Map.entry("COCRDLIC", "CC01"),
        Map.entry("COCRDSLC", "CC02"),
        Map.entry("COCRDUPC", "CC03"),
        Map.entry("COTRN00C", "CT00"),
        Map.entry("COTRN01C", "CT01"),
        Map.entry("COTRN02C", "CT02"),
        Map.entry("CORPT00C", "CR00"),
        Map.entry("COBIL00C", "CB00"),
        Map.entry("COUSR00C", "CU00"),
        Map.entry("COUSR01C", "CU01"),
        Map.entry("COUSR02C", "CU02"),
        Map.entry("COUSR03C", "CU03")
    );

    /**
     * EXEC CICS XCTL PROGRAM('name') -> redirect to URL
     */
    public String xctl(String programName) {
        String url = PROGRAM_TO_URL.get(programName);
        if (url == null) {
            return "/menu";
        }
        return "redirect:" + url;
    }

    /**
     * Get URL path for a program name.
     */
    public String getUrlForProgram(String programName) {
        return PROGRAM_TO_URL.getOrDefault(programName, "/menu");
    }

    /**
     * Get transaction ID for a program name.
     */
    public String getTranIdForProgram(String programName) {
        return PROGRAM_TO_TRANID.getOrDefault(programName, "????");
    }

    /**
     * Check if a program is installed/available.
     */
    public boolean isProgramInstalled(String programName) {
        return PROGRAM_TO_URL.containsKey(programName);
    }
}
