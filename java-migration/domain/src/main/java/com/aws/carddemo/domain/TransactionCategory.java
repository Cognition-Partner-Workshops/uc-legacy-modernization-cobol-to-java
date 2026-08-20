package com.aws.carddemo.domain;
import jakarta.persistence.*;
@Entity @Table(name="transaction_category")
public class TransactionCategory { @EmbeddedId TransactionCategoryId id; @Column(name="cat_type_desc",length=50) String catTypeDesc; }
