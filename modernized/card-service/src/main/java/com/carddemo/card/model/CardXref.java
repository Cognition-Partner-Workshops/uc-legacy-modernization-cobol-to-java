package com.carddemo.card.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_xref", schema = "card")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardXref {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "cust_id", length = 9, nullable = false)
    private String custId;

    @Column(name = "acct_id", length = 11, nullable = false)
    private String acctId;
}
