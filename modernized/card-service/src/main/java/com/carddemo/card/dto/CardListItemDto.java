package com.carddemo.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardListItemDto {
    private String acctId;
    private String cardNum;
    private Character activeStatus;
}
