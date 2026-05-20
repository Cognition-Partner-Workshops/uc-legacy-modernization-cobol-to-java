package com.carddemo.account.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAdjustmentRequest {

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    private String description;
}
