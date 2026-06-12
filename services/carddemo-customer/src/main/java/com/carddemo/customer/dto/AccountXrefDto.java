package com.carddemo.customer.dto;

/**
 * DTO representing a linked account via the XREF cross-reference.
 * Card number is masked to show only the last 4 digits (PCI-DSS compliance).
 */
public class AccountXrefDto {

    private Long accountId;
    private String cardNumberMasked;

    public AccountXrefDto() {
    }

    public AccountXrefDto(Long accountId, String cardNumber) {
        this.accountId = accountId;
        this.cardNumberMasked = maskCardNumber(cardNumber);
    }

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "************";
        }
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }
}
