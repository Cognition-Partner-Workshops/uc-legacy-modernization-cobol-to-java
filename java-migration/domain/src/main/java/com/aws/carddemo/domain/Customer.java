package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="customer")
public class Customer {
 @Id @Column(name="cust_id") Integer custId; @Column(name="first_name",length=25) String firstName; @Column(name="middle_name",length=25) String middleName; @Column(name="last_name",length=25) String lastName;
 @Column(name="addr_line_1",length=50) String addrLine1; @Column(name="addr_line_2",length=50) String addrLine2; @Column(name="addr_line_3",length=50) String addrLine3; @Column(name="addr_state_cd",length=2) String addrStateCd; @Column(name="addr_country_cd",length=3) String addrCountryCd; @Column(name="addr_zip",length=10) String addrZip;
 @Column(name="phone_num_1",length=15) String phoneNum1; @Column(name="phone_num_2",length=15) String phoneNum2; @Column(name="ssn") Integer ssn; @Column(name="govt_issued_id",length=20) String govtIssuedId; @Column(name="dob_yyyy_mm_dd",length=10) String dobYyyyMmDd; @Column(name="eft_account_id",length=10) String eftAccountId; @Column(name="pri_card_holder_ind",length=1) String priCardHolderInd; @Column(name="fico_credit_score") Integer ficoCreditScore;
}
