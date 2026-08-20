package com.aws.carddemo.domain;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="daily_transaction")
public class DailyTransaction {
 @Id @Column(name="tran_id",length=16) String tranId; @Column(name="type_cd",length=2) String typeCd; @Column(name="cat_cd") Integer catCd; @Column(name="source",length=10) String source; @Column(name="tran_desc",length=100) String tranDesc; @Column(name="amt",precision=11,scale=2) BigDecimal amt; @Column(name="merchant_id") Integer merchantId; @Column(name="merchant_name",length=50) String merchantName; @Column(name="merchant_city",length=50) String merchantCity; @Column(name="merchant_zip",length=10) String merchantZip; @Column(name="card_num",length=16) String cardNum; @Column(name="orig_ts",length=26) String origTs; @Column(name="proc_ts",length=26) String procTs;
}
