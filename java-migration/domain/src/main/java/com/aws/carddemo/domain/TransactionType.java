package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="transaction_type")
public class TransactionType { @Id @Column(name="type_cd",length=2) String typeCd; @Column(name="type_desc",length=50) String typeDesc; public String getTypeCd(){return typeCd;} public void setTypeCd(String v){typeCd=v;} public String getTypeDesc(){return typeDesc;} public void setTypeDesc(String v){typeDesc=v;} }
