package com.suitecrm.quotes.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteDto {
    private UUID id;
    private String name;
    private String quoteNum;
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
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
