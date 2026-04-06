package com.suitecrm.contact.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadConvertRequest {
    private UUID leadId;
    private boolean createContact;
    private boolean createAccount;
    private boolean createOpportunity;
    private String accountName;
    private String opportunityName;
    private UUID assignedUserId;
}
