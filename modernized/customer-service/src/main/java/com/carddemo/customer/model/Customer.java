package com.carddemo.customer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", schema = "customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(name = "cust_id", length = 9)
    private String custId;

    @Column(name = "first_name", length = 25, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25, nullable = false)
    private String lastName;

    @Column(name = "addr_line_1", length = 50)
    private String addrLine1;

    @Column(name = "addr_line_2", length = 50)
    private String addrLine2;

    @Column(name = "addr_line_3", length = 50)
    private String addrLine3;

    @Column(name = "addr_state_cd", columnDefinition = "CHAR(2)")
    private String addrStateCd;

    @Column(name = "addr_country_cd", columnDefinition = "CHAR(3)")
    private String addrCountryCd;

    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    @Column(name = "phone_num_1", length = 15)
    private String phoneNum1;

    @Column(name = "phone_num_2", length = 15)
    private String phoneNum2;

    @Column(name = "ssn", length = 9)
    private String ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", columnDefinition = "CHAR(1)")
    private String priCardHolderInd;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
