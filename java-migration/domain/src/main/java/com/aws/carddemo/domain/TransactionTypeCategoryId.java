package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

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

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof TransactionTypeCategoryId that)) return false;
    return Objects.equals(typeCode, that.typeCode) && Objects.equals(category, that.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeCode, category);
  }
}
