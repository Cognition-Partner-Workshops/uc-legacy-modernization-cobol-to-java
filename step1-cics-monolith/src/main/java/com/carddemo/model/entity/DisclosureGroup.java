package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Mirrors CVTRA02Y copybook (DIS-GROUP-RECORD).
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer disTranCatCd;

    @Column(name = "dis_int_rate", precision = 7, scale = 3)
    private BigDecimal disIntRate;

    public DisclosureGroup() {}

    public String getDisAcctGroupId() { return disAcctGroupId; }
    public void setDisAcctGroupId(String disAcctGroupId) { this.disAcctGroupId = disAcctGroupId; }

    public String getDisTranTypeCd() { return disTranTypeCd; }
    public void setDisTranTypeCd(String disTranTypeCd) { this.disTranTypeCd = disTranTypeCd; }

    public Integer getDisTranCatCd() { return disTranCatCd; }
    public void setDisTranCatCd(Integer disTranCatCd) { this.disTranCatCd = disTranCatCd; }

    public BigDecimal getDisIntRate() { return disIntRate; }
    public void setDisIntRate(BigDecimal disIntRate) { this.disIntRate = disIntRate; }

    public static class DisclosureGroupId implements Serializable {
        private String disAcctGroupId;
        private String disTranTypeCd;
        private Integer disTranCatCd;

        public DisclosureGroupId() {}

        public DisclosureGroupId(String disAcctGroupId, String disTranTypeCd, Integer disTranCatCd) {
            this.disAcctGroupId = disAcctGroupId;
            this.disTranTypeCd = disTranTypeCd;
            this.disTranCatCd = disTranCatCd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DisclosureGroupId that = (DisclosureGroupId) o;
            return Objects.equals(disAcctGroupId, that.disAcctGroupId) &&
                   Objects.equals(disTranTypeCd, that.disTranTypeCd) &&
                   Objects.equals(disTranCatCd, that.disTranCatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd);
        }
    }
}
