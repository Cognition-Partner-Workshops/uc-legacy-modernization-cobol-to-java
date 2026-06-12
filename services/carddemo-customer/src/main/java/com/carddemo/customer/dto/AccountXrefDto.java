package com.carddemo.customer.dto;

/**
 * DTO representing a linked account via the XREF cross-reference.
 */
public class AccountXrefDto {

    private Long accountId;
    private String cardNumber;

    public AccountXrefDto() {
    }

    public AccountXrefDto(Long accountId, String cardNumber) {
        this.accountId = accountId;
        this.cardNumber = cardNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
