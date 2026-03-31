package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    @Column(name = "curr_bal", precision = 12, scale = 2)
    private BigDecimal currBal;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    @Column(name = "open_date", length = 10)
    private String openDate;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "reissue_date", length = 10)
    private String reissueDate;

    @Column(name = "curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currCycCredit;

    @Column(name = "curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currCycDebit;

    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    @Column(name = "group_id", length = 10)
    private String groupId;
}
