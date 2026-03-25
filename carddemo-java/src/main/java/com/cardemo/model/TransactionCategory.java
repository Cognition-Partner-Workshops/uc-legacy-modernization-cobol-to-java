package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Transaction Category entity - converted from COBOL copybook CVTRA04Y.cpy
 * Original VSAM record length: 60 bytes
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategoryKey.class)
public class TransactionCategory {

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "tran_cat_cd")
    private Integer catCd;

    @Column(name = "tran_cat_type_desc", length = 50)
    private String description;

    public TransactionCategory() {}

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
