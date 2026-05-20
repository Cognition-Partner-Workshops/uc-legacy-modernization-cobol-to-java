package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_balances", schema = "transaction")
@IdClass(TransactionCategoryBalance.TranCatBalanceId.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionCategoryBalance {
    @Id @Column(name = "acct_id", length = 11) private String acctId;
    @Id @Column(name = "type_cd", columnDefinition = "CHAR(2)") private String typeCd;
    @Id @Column(name = "cat_cd") private Integer catCd;
    @Column(name = "balance", precision = 11, scale = 2) private BigDecimal balance;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TranCatBalanceId implements Serializable {
        private String acctId;
        private String typeCd;
        private Integer catCd;
    }
}
