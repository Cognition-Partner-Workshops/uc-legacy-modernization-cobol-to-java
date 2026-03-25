package com.cardemo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for DisclosureGroup entity.
 */
public class DisclosureGroupKey implements Serializable {

    private String acctGroupId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public DisclosureGroupKey() {}

    public DisclosureGroupKey(String acctGroupId, String tranTypeCd, Integer tranCatCd) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupKey that = (DisclosureGroupKey) o;
        return Objects.equals(acctGroupId, that.acctGroupId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
    }
}
