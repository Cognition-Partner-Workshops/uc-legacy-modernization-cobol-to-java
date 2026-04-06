package com.suitecrm.quotes.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractDto {
    private UUID id;
    private String name;
    private String referenceCode;
    private String status;
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalContractValue;
    private UUID accountId;
    private UUID contactId;
    private UUID opportunityId;
    private UUID assignedUserId;
    private String description;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
