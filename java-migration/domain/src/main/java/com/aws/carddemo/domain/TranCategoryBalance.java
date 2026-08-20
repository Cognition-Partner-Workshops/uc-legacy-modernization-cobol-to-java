package com.aws.carddemo.domain;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="tran_category_balance")
public class TranCategoryBalance { @EmbeddedId TranCategoryBalanceId id; @Column(name="tran_cat_bal",precision=11,scale=2) BigDecimal tranCatBal; }
