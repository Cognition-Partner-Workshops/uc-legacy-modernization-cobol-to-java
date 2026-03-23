package com.carddemo.controller;

import com.carddemo.dto.ReportRequest;
import com.carddemo.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Replaces CR00 report transaction (CORPT00C.cbl).
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String reportForm(Model model) {
        model.addAttribute("reportRequest", new ReportRequest());
        return "report";
    }

    @PostMapping
    public String generateReport(@ModelAttribute ReportRequest request, Model model) {
        if (request.getAcctId() != null && !request.getAcctId().isBlank()) {
            Map<String, Object> report = reportService.generateTransactionReport(
                    request.getAcctId(), request.getStartDate(), request.getEndDate());
            model.addAttribute("report", report);
        }
        model.addAttribute("reportRequest", request);
        return "report";
    }
}
