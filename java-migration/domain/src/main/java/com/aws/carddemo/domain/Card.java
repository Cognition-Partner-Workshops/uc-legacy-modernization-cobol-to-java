package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="card")
public class Card {
 @Id @Column(name="card_num",length=16) String cardNum; @Column(name="acct_id") Long acctId; @Column(name="cvv_cd") Integer cvvCd; @Column(name="embossed_name",length=50) String embossedName; @Column(name="expiraion_date",length=10) String expiraionDate; @Column(name="active_status",length=1) String activeStatus;
}
