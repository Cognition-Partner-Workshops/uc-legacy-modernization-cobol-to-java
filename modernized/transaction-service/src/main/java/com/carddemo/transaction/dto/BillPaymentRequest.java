package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BillPaymentRequest {
    @NotBlank(message = "acctId is required") private String acctId;
}
