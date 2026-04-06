package com.suitecrm.quotes.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceDto {
    private UUID id;
    private String name;
    private String invoiceNumber;
    private UUID quoteId;
    private String status;
    private LocalDate dueDate;
    private BigDecimal subtotalAmount;
    private BigDecimal totalAmount;
    private UUID accountId;
    private UUID contactId;
    private UUID assignedUserId;
    private String description;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
