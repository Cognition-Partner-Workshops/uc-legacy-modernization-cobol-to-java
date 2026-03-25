package com.carddemo.qa.util;

import com.carddemo.qa.model.AccountRecord;
import com.carddemo.qa.model.CardRecord;
import com.carddemo.qa.model.TransactionRecord;
import com.carddemo.qa.model.UserSecurity;

import java.math.BigDecimal;

/**
 * Factory class for creating test data instances used across the QA test suite.
 * Provides consistent, reusable test fixtures based on CardDemo sample data.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UserSecurity createAdminUser() {
        return new UserSecurity("ADMIN001", "ADMIN", "USER", "PASSWORD", "A");
    }

    public static UserSecurity createRegularUser() {
        return new UserSecurity("USER0001", "REGULAR", "USER", "PASSWORD", "U");
    }

    public static UserSecurity createUserWithBlankId() {
        return new UserSecurity("", "TEST", "USER", "PASSWORD", "U");
    }

    public static UserSecurity createUserWithBlankPassword() {
        return new UserSecurity("USER0001", "TEST", "USER", "", "U");
    }

    public static AccountRecord createActiveAccount() {
        AccountRecord account = new AccountRecord();
        account.setAccountId(12345678901L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("3000.00"));
        account.setOpenDate("2020-01-15");
        account.setExpirationDate("2025-01-15");
        account.setReissueDate("2023-01-15");
        account.setCurrentCycleCredit(new BigDecimal("1500.00"));
        account.setCurrentCycleDebit(new BigDecimal("800.00"));
        account.setAddressZip("10001");
        account.setGroupId("GRP001");
        return account;
    }

    public static AccountRecord createInactiveAccount() {
        AccountRecord account = createActiveAccount();
        account.setActiveStatus("N");
        return account;
    }

    public static AccountRecord createAccountWithZeroBalance() {
        AccountRecord account = createActiveAccount();
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        return account;
    }

    public static CardRecord createActiveCard() {
        return new CardRecord(
                "4111111111111111",
                12345678901L,
                123,
                "JOHN DOE",
                "2025-12-31",
                "Y"
        );
    }

    public static CardRecord createInactiveCard() {
        return new CardRecord(
                "4222222222222222",
                12345678901L,
                456,
                "JANE DOE",
                "2022-06-30",
                "N"
        );
    }

    public static TransactionRecord createCreditTransaction() {
        TransactionRecord txn = new TransactionRecord();
        txn.setTransactionId("0000000000000001");
        txn.setTypeCode("01");
        txn.setCategoryCode(5001);
        txn.setSource("ONLINE");
        txn.setDescription("Payment received");
        txn.setAmount(new BigDecimal("250.00"));
        txn.setMerchantId(100000001L);
        txn.setMerchantName("Test Merchant");
        txn.setMerchantCity("New York");
        txn.setMerchantZip("10001");
        txn.setCardNumber("4111111111111111");
        txn.setOriginTimestamp("2024-01-15-10.30.00.000000");
        txn.setProcessedTimestamp("2024-01-15-10.30.05.000000");
        return txn;
    }

    public static TransactionRecord createDebitTransaction() {
        TransactionRecord txn = new TransactionRecord();
        txn.setTransactionId("0000000000000002");
        txn.setTypeCode("02");
        txn.setCategoryCode(5002);
        txn.setSource("POS");
        txn.setDescription("Purchase at store");
        txn.setAmount(new BigDecimal("-150.50"));
        txn.setMerchantId(100000002L);
        txn.setMerchantName("Retail Store");
        txn.setMerchantCity("Los Angeles");
        txn.setMerchantZip("90001");
        txn.setCardNumber("4111111111111111");
        txn.setOriginTimestamp("2024-01-16-14.00.00.000000");
        txn.setProcessedTimestamp("2024-01-16-14.00.03.000000");
        return txn;
    }
}
