package com.carddemo.controller.api;

import com.carddemo.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    private final ReportService reportService;

    public ReportApiController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(required = false) Long acctId,
            @RequestParam(required = false) String cardNum,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> reportData = reportService.generateTransactionReport(acctId, cardNum, startDate, endDate);
        return ResponseEntity.ok(reportData);
    }
}
