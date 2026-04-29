package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_cross_references")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCrossReference {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    @NotBlank
    @Size(max = 16)
    private String cardNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
