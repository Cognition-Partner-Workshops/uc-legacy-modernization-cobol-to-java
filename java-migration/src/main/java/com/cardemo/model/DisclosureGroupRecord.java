/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Disclosure Group entity - migrated from COBOL copybook CVTRA02Y.cpy.
 * Original COBOL record length: 50 bytes.
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupRecord.DisGroupKey.class)
public class DisclosureGroupRecord {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private int disTranCatCd;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal disIntRate;

    public DisclosureGroupRecord() {}

    public String getDisAcctGroupId() { return disAcctGroupId; }
    public void setDisAcctGroupId(String disAcctGroupId) { this.disAcctGroupId = disAcctGroupId; }
    public String getDisTranTypeCd() { return disTranTypeCd; }
    public void setDisTranTypeCd(String disTranTypeCd) { this.disTranTypeCd = disTranTypeCd; }
    public int getDisTranCatCd() { return disTranCatCd; }
    public void setDisTranCatCd(int disTranCatCd) { this.disTranCatCd = disTranCatCd; }
    public BigDecimal getDisIntRate() { return disIntRate; }
    public void setDisIntRate(BigDecimal disIntRate) { this.disIntRate = disIntRate; }

    public static class DisGroupKey implements Serializable {
        private String disAcctGroupId;
        private String disTranTypeCd;
        private int disTranCatCd;

        public DisGroupKey() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisGroupKey that = (DisGroupKey) o;
            return disTranCatCd == that.disTranCatCd &&
                    Objects.equals(disAcctGroupId, that.disAcctGroupId) &&
                    Objects.equals(disTranTypeCd, that.disTranTypeCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd);
        }
    }
}
