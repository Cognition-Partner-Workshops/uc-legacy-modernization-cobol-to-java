package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_groups", schema = "transaction")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DisclosureGroup {
    @Id @Column(name = "acct_group_id", length = 10) private String acctGroupId;
    @Id @Column(name = "tran_type_cd", columnDefinition = "CHAR(2)") private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;
    @Column(name = "int_rate", precision = 6, scale = 2) private BigDecimal intRate;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class DisclosureGroupId implements Serializable {
        private String acctGroupId;
        private String tranTypeCd;
        private Integer tranCatCd;
    }
}
