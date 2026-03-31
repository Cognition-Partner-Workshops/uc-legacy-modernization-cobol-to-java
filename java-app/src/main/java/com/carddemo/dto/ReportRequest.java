package com.carddemo.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String startDate;
    private String endDate;
    private Long acctId;
    private String cardNum;
}
