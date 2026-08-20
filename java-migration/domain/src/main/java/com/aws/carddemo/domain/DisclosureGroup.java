package com.aws.carddemo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_group")
public class DisclosureGroup {
  @EmbeddedId DisclosureGroupId id;

  @Column(name = "int_rate", precision = 6, scale = 2)
  BigDecimal intRate;

  public DisclosureGroupId getId() {
    return id;
  }

  public void setId(DisclosureGroupId v) {
    id = v;
  }

  public BigDecimal getIntRate() {
    return intRate;
  }

  public void setIntRate(BigDecimal v) {
    intRate = v;
  }
}
