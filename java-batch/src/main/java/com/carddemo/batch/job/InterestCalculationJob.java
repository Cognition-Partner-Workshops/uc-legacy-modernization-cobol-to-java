package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DisclosureGroupRecord;
import com.carddemo.batch.model.TranCatBalRecord;
import com.carddemo.batch.model.TransactionRecord;
import com.carddemo.batch.util.TimestampUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of COBOL program CBACT04C.CBL - Interest Calculator.
 *
 * Reads transaction category balance records sequentially, looks up the
 * corresponding disclosure group to get the interest rate, computes monthly
 * interest, and updates the account's current balance. Also generates
 * interest transaction records.
 *
 * Processing flow (from COBOL PROCEDURE DIVISION):
 *   1. Read TCATBAL records sequentially
 *   2. When account changes, update previous account's balance
 *   3. For each category balance, look up interest rate from disclosure group
 *   4. Compute monthly interest: (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 *   5. Write interest transaction record
 *   6. Fees computation is a stub (1400-COMPUTE-FEES: "To be implemented")
 *
 * @param parmDate The date parameter passed via JCL PARM (equivalent to EXTERNAL-PARMS)
 */
public class InterestCalculationJob {

    private final List<TranCatBalRecord> tranCatBalRecords;
    private final Map<Long, CardXrefRecord> xrefByAcctId;
    private final Map<Long, AccountRecord> accountsByAcctId;
    private final Map<String, DisclosureGroupRecord> disclosureGroupByKey;

    private final List<TransactionRecord> interestTransactions = new ArrayList<>();
    private int recordCount = 0;
    private int tranIdSuffix = 0;

    public InterestCalculationJob(
            List<TranCatBalRecord> tranCatBalRecords,
            Map<Long, CardXrefRecord> xrefByAcctId,
            Map<Long, AccountRecord> accountsByAcctId,
            Map<String, DisclosureGroupRecord> disclosureGroupByKey) {
        this.tranCatBalRecords = tranCatBalRecords;
        this.xrefByAcctId = xrefByAcctId;
        this.accountsByAcctId = accountsByAcctId;
        this.disclosureGroupByKey = disclosureGroupByKey;
    }

    /**
     * Execute the interest calculation batch job.
     * Equivalent to COBOL PROCEDURE DIVISION main logic.
     */
    public int execute(String parmDate) {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT04C (Java)");

        long lastAcctNum = -1;
        BigDecimal totalInt = BigDecimal.ZERO;
        boolean firstTime = true;

        for (TranCatBalRecord catBal : tranCatBalRecords) {
            recordCount++;
            System.out.println(catBal);

            // When account changes, update previous account
            if (catBal.getAcctId() != lastAcctNum) {
                if (!firstTime) {
                    updateAccount(lastAcctNum, totalInt);
                } else {
                    firstTime = false;
                }
                totalInt = BigDecimal.ZERO;
                lastAcctNum = catBal.getAcctId();
            }

            // Look up interest rate from disclosure group
            AccountRecord account = accountsByAcctId.get(catBal.getAcctId());
            if (account == null) {
                System.out.println("ACCOUNT NOT FOUND: " + catBal.getAcctId());
                continue;
            }

            BigDecimal interestRate = getInterestRate(
                    account.getGroupId(), catBal.getTypeCd(), catBal.getCatCd());

            if (interestRate.compareTo(BigDecimal.ZERO) != 0) {
                // 1300-COMPUTE-INTEREST: (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
                BigDecimal monthlyInt = catBal.getBalance()
                        .multiply(interestRate)
                        .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

                totalInt = totalInt.add(monthlyInt);

                // 1300-B-WRITE-TX: Write interest transaction
                CardXrefRecord xref = xrefByAcctId.get(catBal.getAcctId());
                writeInterestTransaction(parmDate, monthlyInt, catBal.getAcctId(),
                        xref != null ? xref.getCardNum() : "");

                // 1400-COMPUTE-FEES: stub - "To be implemented" in original COBOL
            }
        }

        // Update the last account
        if (!firstTime) {
            updateAccount(lastAcctNum, totalInt);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT04C (Java)");
        return 0;
    }

    /**
     * Look up interest rate from disclosure group.
     * Equivalent to COBOL 1200-GET-INTEREST-RATE and 1200-A-GET-DEFAULT-INT-RATE.
     */
    BigDecimal getInterestRate(String acctGroupId, String tranTypeCd, int tranCatCd) {
        String key = String.format("%-10s%2s%04d", acctGroupId, tranTypeCd, tranCatCd);
        DisclosureGroupRecord discGroup = disclosureGroupByKey.get(key);

        if (discGroup != null) {
            return discGroup.getInterestRate();
        }

        // Try with DEFAULT group (equivalent to 1200-A-GET-DEFAULT-INT-RATE)
        String defaultKey = String.format("%-10s%2s%04d", "DEFAULT", tranTypeCd, tranCatCd);
        DisclosureGroupRecord defaultGroup = disclosureGroupByKey.get(defaultKey);

        if (defaultGroup != null) {
            return defaultGroup.getInterestRate();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Update account balance with accumulated interest.
     * Equivalent to COBOL 1050-UPDATE-ACCOUNT.
     */
    private void updateAccount(long acctId, BigDecimal totalInt) {
        AccountRecord account = accountsByAcctId.get(acctId);
        if (account != null) {
            account.setCurrBal(account.getCurrBal().add(totalInt));
            account.setCurrCycCredit(BigDecimal.ZERO);
            account.setCurrCycDebit(BigDecimal.ZERO);
        }
    }

    /**
     * Write an interest transaction record.
     * Equivalent to COBOL 1300-B-WRITE-TX.
     */
    private void writeInterestTransaction(String parmDate, BigDecimal monthlyInt,
                                          long acctId, String cardNum) {
        tranIdSuffix++;

        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(parmDate + String.format("%06d", tranIdSuffix));
        tran.setTypeCd("01");
        tran.setCatCd(5);
        tran.setSource("System");
        tran.setDescription("Int. for a/c " + acctId);
        tran.setAmount(monthlyInt);
        tran.setMerchantId(0);
        tran.setMerchantName("");
        tran.setMerchantCity("");
        tran.setMerchantZip("");
        tran.setCardNum(cardNum);

        String timestamp = TimestampUtil.getDb2FormatTimestamp();
        tran.setOrigTimestamp(timestamp);
        tran.setProcTimestamp(timestamp);

        interestTransactions.add(tran);
    }

    public List<TransactionRecord> getInterestTransactions() { return interestTransactions; }
    public int getRecordCount() { return recordCount; }
}
