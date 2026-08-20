package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class TransactionTypeCategoryId implements Serializable {
  @Column(name = "trc_type_code", length = 2)
  String typeCode;

  @Column(name = "trc_type_category", length = 4)
  String category;

  public TransactionTypeCategoryId() {}

  public TransactionTypeCategoryId(String typeCode, String category) {
    this.typeCode = typeCode;
    this.category = category;
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String value) {
    typeCode = value;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    category = value;
  }
}
