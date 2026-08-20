package com.aws.carddemo.domain;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="account")
public class Account {
 @Id @Column(name="acct_id") Long acctId; @Column(name="active_status",length=1) String activeStatus;
 @Column(name="curr_bal",precision=12,scale=2) BigDecimal currBal; @Column(name="credit_limit",precision=12,scale=2) BigDecimal creditLimit; @Column(name="cash_credit_limit",precision=12,scale=2) BigDecimal cashCreditLimit;
 @Column(name="open_date",length=10) String openDate; @Column(name="expiraion_date",length=10) String expiraionDate; @Column(name="reissue_date",length=10) String reissueDate;
 @Column(name="curr_cyc_credit",precision=12,scale=2) BigDecimal currCycCredit; @Column(name="curr_cyc_debit",precision=12,scale=2) BigDecimal currCycDebit; @Column(name="addr_zip",length=10) String addrZip; @Column(name="group_id",length=10) String groupId;
}
