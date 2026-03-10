package com.carddemo.common.model;

/**
 * Maps COBOL copybook CVCUS01Y - Customer entity (RECLN 500).
 *
 * <pre>
 * 05  CUST-ID                    PIC 9(09)
 * 05  CUST-FIRST-NAME            PIC X(25)
 * 05  CUST-MIDDLE-NAME           PIC X(25)
 * 05  CUST-LAST-NAME             PIC X(25)
 * 05  CUST-ADDR-LINE-1           PIC X(50)
 * 05  CUST-ADDR-LINE-2           PIC X(50)
 * 05  CUST-ADDR-LINE-3           PIC X(50)
 * 05  CUST-ADDR-STATE-CD         PIC X(02)
 * 05  CUST-ADDR-COUNTRY-CD       PIC X(03)
 * 05  CUST-ADDR-ZIP              PIC X(10)
 * 05  CUST-PHONE-NUM-1           PIC X(15)
 * 05  CUST-PHONE-NUM-2           PIC X(15)
 * 05  CUST-SSN                   PIC 9(09)
 * 05  CUST-GOVT-ISSUED-ID        PIC X(20)
 * 05  CUST-DOB-YYYY-MM-DD        PIC X(10)
 * 05  CUST-EFT-ACCOUNT-ID        PIC X(10)
 * 05  CUST-PRI-CARD-HOLDER-IND   PIC X(01)
 * 05  CUST-FICO-CREDIT-SCORE     PIC 9(03)
 * 05  FILLER                     PIC X(168)
 * </pre>
 */
public record CustomerRecord(
        String custId,              // PIC 9(09)
        String firstName,           // PIC X(25)
        String middleName,          // PIC X(25)
        String lastName,            // PIC X(25)
        String addrLine1,           // PIC X(50)
        String addrLine2,           // PIC X(50)
        String addrLine3,           // PIC X(50)
        String addrStateCd,         // PIC X(02)
        String addrCountryCd,       // PIC X(03)
        String addrZip,             // PIC X(10)
        String phoneNum1,           // PIC X(15)
        String phoneNum2,           // PIC X(15)
        String ssn,                 // PIC 9(09)
        String govtIssuedId,        // PIC X(20)
        String dobYyyyMmDd,         // PIC X(10)
        String eftAccountId,        // PIC X(10)
        String priCardHolderInd,    // PIC X(01)
        String ficoCreditScore      // PIC 9(03)
) {
    public static final int RECORD_LENGTH = 500;
    public static final int CUST_ID_LEN = 9;
    public static final int FIRST_NAME_LEN = 25;
    public static final int MIDDLE_NAME_LEN = 25;
    public static final int LAST_NAME_LEN = 25;
    public static final int ADDR_LINE_LEN = 50;
    public static final int STATE_CD_LEN = 2;
    public static final int COUNTRY_CD_LEN = 3;
    public static final int ZIP_LEN = 10;
    public static final int PHONE_LEN = 15;
    public static final int SSN_LEN = 9;
    public static final int GOVT_ID_LEN = 20;
    public static final int DOB_LEN = 10;
    public static final int EFT_ACCT_LEN = 10;
    public static final int PRI_CARD_IND_LEN = 1;
    public static final int FICO_LEN = 3;
    public static final int FILLER_LEN = 168;
}
