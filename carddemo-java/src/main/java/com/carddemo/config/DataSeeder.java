package com.carddemo.config;

import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.User;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds the database with sample data matching the original mainframe test data.
 * Only runs if the users table is empty (first startup).
 * Based on app/data/ASCII/ sample data files.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "PASSWORD";

    @Bean
    public CommandLineRunner seedData(UserRepository userRepo,
                                       AccountRepository accountRepo,
                                       CustomerRepository customerRepo,
                                       CardRepository cardRepo,
                                       CardXrefRepository xrefRepo,
                                       TransactionRepository tranRepo,
                                       TranCatBalanceRepository balRepo,
                                       PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) {
                log.info("Database already seeded, skipping.");
                return;
            }

            log.info("Seeding database with sample CardDemo data...");
            String hash = encoder.encode(DEFAULT_PASSWORD);

            // Users (from USRSEC VSAM file)
            seedUser(userRepo, "ADMIN001", "ADMIN", "USER", hash, "A");
            seedUser(userRepo, "USER0001", "FIRST01", "LAST01", hash, "U");
            seedUser(userRepo, "USER0002", "FIRST02", "LAST02", hash, "U");
            seedUser(userRepo, "USER0003", "FIRST03", "LAST03", hash, "U");

            // Accounts (from ACCTDAT VSAM file)
            seedAccount(accountRepo, 10000000001L, "Y", "5000.00", "15000.00", "5000.00",
                    "2020-01-15", "2025-12-31", "2024-01-15", "1200.00", "800.00", "10101", "GRP001");
            seedAccount(accountRepo, 10000000002L, "Y", "12500.50", "25000.00", "10000.00",
                    "2019-06-20", "2025-06-30", "2024-06-20", "3500.00", "2100.00", "20202", "GRP001");
            seedAccount(accountRepo, 10000000003L, "Y", "750.25", "10000.00", "3000.00",
                    "2021-03-10", "2026-03-31", "2025-03-10", "500.00", "250.00", "30303", "GRP002");
            seedAccount(accountRepo, 10000000004L, "N", "0.00", "5000.00", "2000.00",
                    "2018-11-01", "2023-11-30", null, "0.00", "0.00", "40404", "GRP002");
            seedAccount(accountRepo, 10000000005L, "Y", "8200.00", "20000.00", "8000.00",
                    "2020-08-22", "2025-08-31", "2024-08-22", "2000.00", "1500.00", "50505", "GRP003");

            // Customers (from CUSTDAT VSAM file)
            seedCustomer(customerRepo, 100001L, "JOHN", "A", "SMITH", "123 MAIN ST", "APT 4B", null,
                    "NY", "US", "10101", "2125551234", "2125555678", "123456789", "DL12345",
                    "1985-03-15", "EFT00001", "Y", 750);
            seedCustomer(customerRepo, 100002L, "JANE", "B", "DOE", "456 OAK AVE", null, null,
                    "CA", "US", "20202", "3105551234", null, "234567890", "DL23456",
                    "1990-07-22", "EFT00002", "Y", 680);
            seedCustomer(customerRepo, 100003L, "ROBERT", "C", "JOHNSON", "789 ELM BLVD", "SUITE 100", null,
                    "TX", "US", "30303", "2145551234", "2145555678", "345678901", "DL34567",
                    "1978-11-08", "EFT00003", "Y", 720);
            seedCustomer(customerRepo, 100004L, "MARIA", null, "GARCIA", "321 PINE RD", null, null,
                    "FL", "US", "40404", "3055551234", null, "456789012", "DL45678",
                    "1995-01-30", "EFT00004", "Y", 650);
            seedCustomer(customerRepo, 100005L, "DAVID", "E", "WILSON", "654 MAPLE DR", "BLDG C", "FLOOR 2",
                    "IL", "US", "50505", "3125551234", "3125555678", "567890123", "DL56789",
                    "1982-09-12", "EFT00005", "Y", 800);

            // Cards (from CARDDAT VSAM file)
            seedCard(cardRepo, "4111111111111111", 10000000001L, "123", "JOHN A SMITH", "2025-12-31", "Y");
            seedCard(cardRepo, "4111111111112222", 10000000001L, "456", "JOHN A SMITH", "2025-12-31", "Y");
            seedCard(cardRepo, "4222222222221111", 10000000002L, "789", "JANE B DOE", "2025-06-30", "Y");
            seedCard(cardRepo, "4333333333331111", 10000000003L, "321", "ROBERT C JOHNSON", "2026-03-31", "Y");
            seedCard(cardRepo, "4444444444441111", 10000000004L, "654", "MARIA GARCIA", "2023-11-30", "N");
            seedCard(cardRepo, "4555555555551111", 10000000005L, "987", "DAVID E WILSON", "2025-08-31", "Y");

            // Card cross-references (from CCXREF VSAM file)
            seedXref(xrefRepo, "4111111111111111", 100001L, 10000000001L);
            seedXref(xrefRepo, "4111111111112222", 100001L, 10000000001L);
            seedXref(xrefRepo, "4222222222221111", 100002L, 10000000002L);
            seedXref(xrefRepo, "4333333333331111", 100003L, 10000000003L);
            seedXref(xrefRepo, "4444444444441111", 100004L, 10000000004L);
            seedXref(xrefRepo, "4555555555551111", 100005L, 10000000005L);

            // Transactions (from TRANSACT VSAM file)
            seedTransaction(tranRepo, "0000000000000001", "SA", 5010, "ONLINE", "Purchase - Electronics Store",
                    "299.99", 90001L, "BEST ELECTRONICS", "NEW YORK", "10001", "4111111111111111",
                    "2024-01-15T10:30:00", "2024-01-15T10:30:05");
            seedTransaction(tranRepo, "0000000000000002", "SA", 5020, "ONLINE", "Purchase - Grocery",
                    "85.50", 90002L, "FRESH MART", "NEW YORK", "10002", "4111111111111111",
                    "2024-01-16T14:15:00", "2024-01-16T14:15:03");
            seedTransaction(tranRepo, "0000000000000003", "SA", 5030, "ONLINE", "Purchase - Gas Station",
                    "45.00", 90003L, "QUICK FUEL", "LOS ANGELES", "90001", "4222222222221111",
                    "2024-01-17T08:45:00", "2024-01-17T08:45:02");
            seedTransaction(tranRepo, "0000000000000004", "CA", 5010, "ATM", "Cash Advance",
                    "500.00", null, "ATM WITHDRAWAL", "LOS ANGELES", "90002", "4222222222221111",
                    "2024-01-18T16:00:00", "2024-01-18T16:00:10");
            seedTransaction(tranRepo, "0000000000000005", "SA", 5040, "ONLINE", "Purchase - Restaurant",
                    "62.75", 90004L, "FINE DINING", "HOUSTON", "77001", "4333333333331111",
                    "2024-01-19T19:30:00", "2024-01-19T19:30:04");
            seedTransaction(tranRepo, "0000000000000006", "BP", 5000, "ONLINE", "Bill Payment",
                    "-1000.00", null, null, null, null, "4111111111111111",
                    "2024-01-20T09:00:00", "2024-01-20T09:00:01");
            seedTransaction(tranRepo, "0000000000000007", "SA", 5050, "ONLINE", "Purchase - Department Store",
                    "175.25", 90005L, "MEGA STORE", "CHICAGO", "60601", "4555555555551111",
                    "2024-01-21T12:00:00", "2024-01-21T12:00:06");
            seedTransaction(tranRepo, "0000000000000008", "IC", 9000, "BATCH", "Monthly Interest Charge",
                    "75.00", null, null, null, null, "4222222222221111",
                    "2024-02-01T00:00:00", "2024-02-01T00:00:01");

            // Transaction category balances (from TCATBAL VSAM file)
            balRepo.save(new TranCatBalance(10000000001L, "SA", 5010, new BigDecimal("299.99")));
            balRepo.save(new TranCatBalance(10000000001L, "SA", 5020, new BigDecimal("85.50")));
            balRepo.save(new TranCatBalance(10000000001L, "BP", 5000, new BigDecimal("-1000.00")));
            balRepo.save(new TranCatBalance(10000000002L, "SA", 5030, new BigDecimal("45.00")));
            balRepo.save(new TranCatBalance(10000000002L, "CA", 5010, new BigDecimal("500.00")));
            balRepo.save(new TranCatBalance(10000000002L, "IC", 9000, new BigDecimal("75.00")));
            balRepo.save(new TranCatBalance(10000000003L, "SA", 5040, new BigDecimal("62.75")));
            balRepo.save(new TranCatBalance(10000000005L, "SA", 5050, new BigDecimal("175.25")));

            log.info("Database seeding complete: 4 users, 5 accounts, 5 customers, 6 cards, 8 transactions");
        };
    }

    private void seedUser(UserRepository repo, String userId, String first, String last, String hash, String type) {
        User u = new User();
        u.setUserId(userId);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPasswordHash(hash);
        u.setUserType(type);
        repo.save(u);
    }

    private void seedAccount(AccountRepository repo, Long id, String status, String balance,
                              String creditLimit, String cashLimit, String openDate, String expDate,
                              String reissueDate, String cycleCredit, String cycleDebit, String zip, String group) {
        Account a = new Account();
        a.setAccountId(id);
        a.setActiveStatus(status);
        a.setCurrentBalance(new BigDecimal(balance));
        a.setCreditLimit(new BigDecimal(creditLimit));
        a.setCashCreditLimit(new BigDecimal(cashLimit));
        a.setOpenDate(LocalDate.parse(openDate));
        a.setExpirationDate(expDate != null ? LocalDate.parse(expDate) : null);
        a.setReissueDate(reissueDate != null ? LocalDate.parse(reissueDate) : null);
        a.setCycleCredit(new BigDecimal(cycleCredit));
        a.setCycleDebit(new BigDecimal(cycleDebit));
        a.setZipCode(zip);
        a.setGroupId(group);
        repo.save(a);
    }

    private void seedCustomer(CustomerRepository repo, Long id, String first, String middle, String last,
                               String addr1, String addr2, String addr3, String state, String country,
                               String zip, String phone1, String phone2, String ssn, String govtId,
                               String dob, String eftId, String primary, int fico) {
        Customer c = new Customer();
        c.setCustomerId(id);
        c.setFirstName(first);
        c.setMiddleName(middle);
        c.setLastName(last);
        c.setAddrLine1(addr1);
        c.setAddrLine2(addr2);
        c.setAddrLine3(addr3);
        c.setStateCode(state);
        c.setCountryCode(country);
        c.setZipCode(zip);
        c.setPhone1(phone1);
        c.setPhone2(phone2);
        c.setSsn(ssn);
        c.setGovtId(govtId);
        c.setDateOfBirth(LocalDate.parse(dob));
        c.setEftAccountId(eftId);
        c.setPrimaryHolder(primary);
        c.setFicoScore(fico);
        repo.save(c);
    }

    private void seedCard(CardRepository repo, String cardNum, Long accountId, String cvv,
                           String name, String expDate, String status) {
        Card c = new Card();
        c.setCardNumber(cardNum);
        c.setAccountId(accountId);
        c.setCvvCode(cvv);
        c.setEmbossedName(name);
        c.setExpirationDate(LocalDate.parse(expDate));
        c.setActiveStatus(status);
        repo.save(c);
    }

    private void seedXref(CardXrefRepository repo, String cardNum, Long custId, Long acctId) {
        CardXref x = new CardXref();
        x.setCardNumber(cardNum);
        x.setCustomerId(custId);
        x.setAccountId(acctId);
        repo.save(x);
    }

    private void seedTransaction(TransactionRepository repo, String id, String type, int category,
                                  String source, String desc, String amount, Long merchantId,
                                  String merchantName, String merchantCity, String merchantZip,
                                  String cardNum, String originated, String processed) {
        Transaction t = new Transaction();
        t.setTransactionId(id);
        t.setTypeCode(type);
        t.setCategoryCode(category);
        t.setSource(source);
        t.setDescription(desc);
        t.setAmount(new BigDecimal(amount));
        t.setMerchantId(merchantId);
        t.setMerchantName(merchantName);
        t.setMerchantCity(merchantCity);
        t.setMerchantZip(merchantZip);
        t.setCardNumber(cardNum);
        t.setOriginatedTs(LocalDateTime.parse(originated));
        t.setProcessedTs(LocalDateTime.parse(processed));
        repo.save(t);
    }
}
