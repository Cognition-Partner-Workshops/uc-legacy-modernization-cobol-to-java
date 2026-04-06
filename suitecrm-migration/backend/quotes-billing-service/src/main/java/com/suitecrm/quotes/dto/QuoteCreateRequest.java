package com.suitecrm.quotes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String quoteStage;
    private String paymentTerms;
    private LocalDate validUntil;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;
    private UUID accountId;
    private UUID contactId;
    private UUID opportunityId;
    private UUID assignedUserId;
    private String description;
    private String billingAddressStreet;
    private String billingAddressCity;
    private String billingAddressState;
    private String billingAddressPostalcode;
    private String billingAddressCountry;
    private String shippingAddressStreet;
    private String shippingAddressCity;
    private String shippingAddressState;
    private String shippingAddressPostalcode;
    private String shippingAddressCountry;
}
