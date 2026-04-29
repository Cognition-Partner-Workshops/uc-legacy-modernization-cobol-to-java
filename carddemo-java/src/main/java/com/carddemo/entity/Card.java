package com.carddemo.entity;

import java.time.LocalDate;

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
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    @NotBlank
    @Size(max = 16)
    private String cardNumber;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "cvv_code", length = 3)
    @Size(max = 3)
    private String cvvCode;

    @Column(name = "embossed_name", length = 50)
    @Size(max = 50)
    private String embossedName;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;
}
