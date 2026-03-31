package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "rejected_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "cat_cd")
    private Integer catCd;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "reject_reason_cd")
    private Integer rejectReasonCd;

    @Column(name = "reject_reason", length = 100)
    private String rejectReason;
}
