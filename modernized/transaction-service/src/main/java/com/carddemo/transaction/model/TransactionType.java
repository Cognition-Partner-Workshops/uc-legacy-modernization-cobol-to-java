package com.carddemo.transaction.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tran_types", schema = "transaction")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionType {
    @Id @Column(name = "tran_type", columnDefinition = "CHAR(2)") private String tranType;
    @Column(name = "type_desc", length = 50) private String typeDesc;
}
