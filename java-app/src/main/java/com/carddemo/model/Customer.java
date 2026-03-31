package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "address_line_1", length = 50)
    private String addressLine1;

    @Column(name = "address_line_2", length = 50)
    private String addressLine2;

    @Column(name = "address_line_3", length = 50)
    private String addressLine3;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "phone_1", length = 15)
    private String phone1;

    @Column(name = "phone_2", length = 15)
    private String phone2;

    @Column(name = "ssn", length = 9)
    private String ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "dob", length = 10)
    private String dob;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "pri_card_holder_ind", length = 1)
    private String priCardHolderInd;

    @Column(name = "fico_score")
    private Integer ficoScore;
}
