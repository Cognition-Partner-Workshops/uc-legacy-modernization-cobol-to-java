package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddTransactionRequest {
    @NotBlank(message = "cardNum is required") private String cardNum;
    @NotBlank(message = "typeCd is required") private String typeCd;
    private Integer catCd;
    private String source;
    private String description;
    @NotNull(message = "amount is required") private BigDecimal amount;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
}
