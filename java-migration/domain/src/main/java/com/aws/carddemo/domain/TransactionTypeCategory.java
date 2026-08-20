package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_type_category")
public class TransactionTypeCategory {
  @EmbeddedId TransactionTypeCategoryId id;

  @Column(name = "trc_cat_data", length = 50, nullable = false)
  String data;

  public TransactionTypeCategoryId getId() {
    return id;
  }

  public void setId(TransactionTypeCategoryId value) {
    id = value;
  }

  public String getData() {
    return data;
  }

  public void setData(String value) {
    data = value;
  }
}
