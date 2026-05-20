package com.carddemo.transaction.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionDto {
    private String tranId;
    private String typeCd;
    private Integer catCd;
    private String source;
    private String description;
    private BigDecimal amount;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNum;
    private String origTs;
    private String procTs;
}
