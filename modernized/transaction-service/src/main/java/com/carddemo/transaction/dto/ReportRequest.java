package com.carddemo.transaction.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportRequest {
    private String startDate;
    private String endDate;
    private String acctId;
}
