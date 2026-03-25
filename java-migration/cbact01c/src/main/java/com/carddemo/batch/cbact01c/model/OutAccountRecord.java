package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-FILE record layout.
 *
 * <pre>
 *  05  OUT-ACCT-ID                PIC 9(11)
 *  05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *  05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *  05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *  05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *  05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *  05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *  05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *  05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  USAGE IS COMP-3
 *  05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 */
public record OutAccountRecord(
        long acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpirationDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctGroupId
) {

    /** Default debit value applied when the input cycle debit is zero. */
    public static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");

    /**
     * Build an {@code OutAccountRecord} from the source {@link AccountRecord},
     * applying all COBOL business rules:
     * <ul>
     *   <li>Reissue date converted from {@code YYYY-MM-DD} → {@code YYYYMMDD}</li>
     *   <li>Cycle debit defaulted to 2525.00 when zero</li>
     * </ul>
     */
    public static OutAccountRecord fromAccount(AccountRecord acct, String formattedReissueDate) {
        BigDecimal debit = acct.acctCurrCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_DEBIT
                : acct.acctCurrCycDebit();

        return new OutAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpirationDate(),
                formattedReissueDate,
                acct.acctCurrCycCredit(),
                debit,
                acct.acctGroupId()
        );
    }

    /**
     * Serialize to a pipe-delimited line for the output file.
     * In COBOL the COMP-3 field would be packed-decimal; here we use a
     * human-readable delimited format instead.
     */
    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
                acctActiveStatus,
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctCashCreditLimit.toPlainString(),
                acctOpenDate,
                acctExpirationDate,
                acctReissueDate,
                acctCurrCycCredit.toPlainString(),
                acctCurrCycDebit.toPlainString(),
                acctGroupId
        );
    }
}
