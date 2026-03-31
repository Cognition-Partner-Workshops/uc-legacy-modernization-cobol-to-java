package com.carddemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryBalanceId implements Serializable {
    private Long acctId;
    private String typeCd;
    private Integer catCd;
}
