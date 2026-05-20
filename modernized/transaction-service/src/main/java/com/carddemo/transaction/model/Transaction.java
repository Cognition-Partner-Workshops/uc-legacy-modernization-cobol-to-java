package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions", schema = "transaction")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Transaction {
    @Id @Column(name = "tran_id", length = 16) private String tranId;
    @Column(name = "type_cd", columnDefinition = "CHAR(2)") private String typeCd;
    @Column(name = "cat_cd") private Integer catCd;
    @Column(name = "source", length = 10) private String source;
    @Column(name = "description", length = 100) private String description;
    @Column(name = "amount", precision = 11, scale = 2) private BigDecimal amount;
    @Column(name = "merchant_id", length = 9) private String merchantId;
    @Column(name = "merchant_name", length = 50) private String merchantName;
    @Column(name = "merchant_city", length = 50) private String merchantCity;
    @Column(name = "merchant_zip", length = 10) private String merchantZip;
    @Column(name = "card_num", length = 16) private String cardNum;
    @Column(name = "orig_ts", length = 26) private String origTs;
    @Column(name = "proc_ts", length = 26) private String procTs;
}
