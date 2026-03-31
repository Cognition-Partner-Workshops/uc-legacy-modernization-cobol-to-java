package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionAddRequest {
    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be 16 digits")
    private String cardNum;

    @NotBlank(message = "Transaction type is required")
    @Size(max = 2)
    private String typeCd;

    @NotNull(message = "Category code is required")
    private Integer catCd;

    @Size(max = 10)
    private String source;

    @Size(max = 100)
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private Long merchantId;

    @Size(max = 50)
    private String merchantName;

    @Size(max = 50)
    private String merchantCity;

    @Size(max = 10)
    private String merchantZip;
}
