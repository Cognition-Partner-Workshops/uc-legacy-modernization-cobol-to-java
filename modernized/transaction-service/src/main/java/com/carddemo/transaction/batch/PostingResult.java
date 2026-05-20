package com.carddemo.transaction.batch;

import com.carddemo.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostingResult {

    private boolean valid;
    private Transaction transaction;

    private String typeCd;
    private Integer catCd;
    private String cardNum;
    private BigDecimal amount;

    private String rejectedTranId;
    private String rejectedCardNum;
    private BigDecimal rejectedAmount;
    private Integer rejectReasonCode;
    private String rejectReasonDesc;

    public static PostingResult valid(Transaction txn, String typeCd, Integer catCd,
                                       String cardNum, BigDecimal amount) {
        return PostingResult.builder()
                .valid(true)
                .transaction(txn)
                .typeCd(typeCd)
                .catCd(catCd)
                .cardNum(cardNum)
                .amount(amount)
                .build();
    }

    public static PostingResult rejected(String tranId, String cardNum, BigDecimal amount,
                                          int reasonCode, String reasonDesc) {
        return PostingResult.builder()
                .valid(false)
                .rejectedTranId(tranId)
                .rejectedCardNum(cardNum)
                .rejectedAmount(amount)
                .rejectReasonCode(reasonCode)
                .rejectReasonDesc(reasonDesc)
                .build();
    }
}
