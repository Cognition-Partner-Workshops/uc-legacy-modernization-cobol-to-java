package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionType {

    @Id
    @Column(name = "type_cd", length = 2)
    private String typeCd;

    @Column(name = "type_description", length = 50)
    private String typeDescription;
}
