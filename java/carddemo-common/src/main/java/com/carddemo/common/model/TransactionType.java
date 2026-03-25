package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping for the Transaction Type VSAM record.
 * Maps to COBOL copybook: CVTRA03Y.cpy (60-byte record)
 *
 * COBOL record layout:
 *   TRAN-TYPE       PIC X(02)
 *   TRAN-TYPE-DESC  PIC X(50)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "type_description", length = 50)
    private String typeDescription;
}
