package com.carddemo.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailDto {
    private String cardNum;
    private String acctId;
    private Integer cvvCd;
    private String embossedName;
    private String expirationDate;
    private Character activeStatus;
    private String custId;
    private String customerName;
}
