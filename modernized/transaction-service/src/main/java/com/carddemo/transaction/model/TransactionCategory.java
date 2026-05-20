package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "tran_categories", schema = "transaction")
@IdClass(TransactionCategory.TransactionCategoryId.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionCategory {
    @Id @Column(name = "type_cd", columnDefinition = "CHAR(2)") private String typeCd;
    @Id @Column(name = "cat_cd") private Integer catCd;
    @Column(name = "cat_desc", length = 50) private String catDesc;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TransactionCategoryId implements Serializable {
        private String typeCd;
        private Integer catCd;
    }
}
