package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="card_xref")
public class CardXref {
 @EmbeddedId CardXrefId id; @Column(name="cust_id",nullable=false) Integer custId; @Column(name="acct_id",nullable=false) Long acctId;
}
