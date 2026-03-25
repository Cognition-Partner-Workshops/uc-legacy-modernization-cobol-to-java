package com.carddemo.dto;

import com.carddemo.entity.Account;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Card;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Combined account detail response - consolidates data from COACTVWC which reads
 * ACCTDAT, CUSTDAT, and CARDXREF VSAM files in a single screen display.
 */
public class AccountDetailResponse {

    // Account fields
    private Long accountId;
    private String activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private LocalDate openDate;
    private LocalDate expirationDate;
    private LocalDate reissueDate;
    private BigDecimal cycleCredit;
    private BigDecimal cycleDebit;
    private String zipCode;
    private String groupId;

    // Customer fields
    private Long customerId;
    private String customerFirstName;
    private String customerMiddleName;
    private String customerLastName;
    private String addrLine1;
    private String addrLine2;
    private String addrLine3;
    private String stateCode;
    private String countryCode;
    private String customerZipCode;
    private String phone1;
    private String phone2;
    private Integer ficoScore;

    // Associated cards
    private List<CardSummary> cards;

    public static AccountDetailResponse fromEntities(Account account, Customer customer, List<Card> cards) {
        AccountDetailResponse resp = new AccountDetailResponse();
        resp.accountId = account.getAccountId();
        resp.activeStatus = account.getActiveStatus();
        resp.currentBalance = account.getCurrentBalance();
        resp.creditLimit = account.getCreditLimit();
        resp.cashCreditLimit = account.getCashCreditLimit();
        resp.openDate = account.getOpenDate();
        resp.expirationDate = account.getExpirationDate();
        resp.reissueDate = account.getReissueDate();
        resp.cycleCredit = account.getCycleCredit();
        resp.cycleDebit = account.getCycleDebit();
        resp.zipCode = account.getZipCode();
        resp.groupId = account.getGroupId();

        if (customer != null) {
            resp.customerId = customer.getCustomerId();
            resp.customerFirstName = customer.getFirstName();
            resp.customerMiddleName = customer.getMiddleName();
            resp.customerLastName = customer.getLastName();
            resp.addrLine1 = customer.getAddrLine1();
            resp.addrLine2 = customer.getAddrLine2();
            resp.addrLine3 = customer.getAddrLine3();
            resp.stateCode = customer.getStateCode();
            resp.countryCode = customer.getCountryCode();
            resp.customerZipCode = customer.getZipCode();
            resp.phone1 = customer.getPhone1();
            resp.phone2 = customer.getPhone2();
            resp.ficoScore = customer.getFicoScore();
        }

        if (cards != null) {
            resp.cards = cards.stream().map(CardSummary::fromEntity).toList();
        }

        return resp;
    }

    public Long getAccountId() { return accountId; }
    public String getActiveStatus() { return activeStatus; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public LocalDate getOpenDate() { return openDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public LocalDate getReissueDate() { return reissueDate; }
    public BigDecimal getCycleCredit() { return cycleCredit; }
    public BigDecimal getCycleDebit() { return cycleDebit; }
    public String getZipCode() { return zipCode; }
    public String getGroupId() { return groupId; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerFirstName() { return customerFirstName; }
    public String getCustomerMiddleName() { return customerMiddleName; }
    public String getCustomerLastName() { return customerLastName; }
    public String getAddrLine1() { return addrLine1; }
    public String getAddrLine2() { return addrLine2; }
    public String getAddrLine3() { return addrLine3; }
    public String getStateCode() { return stateCode; }
    public String getCountryCode() { return countryCode; }
    public String getCustomerZipCode() { return customerZipCode; }
    public String getPhone1() { return phone1; }
    public String getPhone2() { return phone2; }
    public Integer getFicoScore() { return ficoScore; }
    public List<CardSummary> getCards() { return cards; }

    public static class CardSummary {
        private String cardNumber;
        private String embossedName;
        private String activeStatus;
        private LocalDate expirationDate;

        public static CardSummary fromEntity(Card card) {
            CardSummary s = new CardSummary();
            s.cardNumber = card.getCardNumber();
            s.embossedName = card.getEmbossedName();
            s.activeStatus = card.getActiveStatus();
            s.expirationDate = card.getExpirationDate();
            return s;
        }

        public String getCardNumber() { return cardNumber; }
        public String getEmbossedName() { return embossedName; }
        public String getActiveStatus() { return activeStatus; }
        public LocalDate getExpirationDate() { return expirationDate; }
    }
}
