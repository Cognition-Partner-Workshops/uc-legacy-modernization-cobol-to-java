package com.cardemo.common;

/**
 * Application constants - migrated from COBOL copybooks COTTL01Y.cpy and CSMSG01Y.cpy.
 * Replaces COBOL 01-level constant definitions.
 */
public final class CardDemoConstants {

    // Screen Titles (from COTTL01Y.cpy)
    public static final String TITLE_01 = "AWS Mainframe Modernization";
    public static final String TITLE_02 = "CardDemo";
    public static final String THANK_YOU_MSG = "Thank you for using CCDA application...";

    // Common Messages (from CSMSG01Y.cpy)
    public static final String MSG_THANK_YOU = "Thank you for using CardDemo application...";
    public static final String MSG_INVALID_KEY = "Invalid key pressed. Please see below...";

    // User Types
    public static final String USER_TYPE_ADMIN = "A";
    public static final String USER_TYPE_USER = "U";

    // Transaction IDs (CICS TRANID equivalents)
    public static final String TRAN_SIGNON = "CC00";
    public static final String TRAN_MAIN_MENU = "CM00";
    public static final String TRAN_ACCT_VIEW = "CAVW";
    public static final String TRAN_ACCT_UPDATE = "CAUP";
    public static final String TRAN_CARD_LIST = "CCLI";
    public static final String TRAN_CARD_VIEW = "CCDL";
    public static final String TRAN_CARD_UPDATE = "CCUP";
    public static final String TRAN_TRAN_LIST = "CT00";
    public static final String TRAN_TRAN_VIEW = "CT01";
    public static final String TRAN_TRAN_ADD = "CT02";
    public static final String TRAN_REPORT = "CR00";
    public static final String TRAN_BILL_PAY = "CB00";

    // Program Names (COBOL program IDs)
    public static final String PGM_SIGNON = "COSGN00C";
    public static final String PGM_MAIN_MENU = "COMEN01C";
    public static final String PGM_ADMIN_MENU = "COADM01C";
    public static final String PGM_ACCT_VIEW = "COACTVWC";
    public static final String PGM_ACCT_UPDATE = "COACTUPC";
    public static final String PGM_CARD_LIST = "COCRDLIC";
    public static final String PGM_CARD_VIEW = "COCRDSLC";
    public static final String PGM_CARD_UPDATE = "COCRDUPC";
    public static final String PGM_TRAN_LIST = "COTRN00C";
    public static final String PGM_TRAN_VIEW = "COTRN01C";
    public static final String PGM_TRAN_ADD = "COTRN02C";
    public static final String PGM_REPORT = "CORPT00C";
    public static final String PGM_BILL_PAY = "COBIL00C";
    public static final String PGM_USER_LIST = "COUSR00C";
    public static final String PGM_USER_ADD = "COUSR01C";
    public static final String PGM_USER_UPDATE = "COUSR02C";
    public static final String PGM_USER_DELETE = "COUSR03C";

    private CardDemoConstants() {
    }
}
