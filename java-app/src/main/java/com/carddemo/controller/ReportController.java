package com.carddemo.controller;

import com.carddemo.dto.ReportRequest;
import com.carddemo.service.ReportService;
import com.carddemo.util.DateTimeUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String showReports(Model model, Authentication authentication) {
        populateHeader(model, authentication);
        model.addAttribute("reportRequest", new ReportRequest());
        return "reports";
    }

    @PostMapping
    public String generateReport(@ModelAttribute ReportRequest reportRequest,
                                  Model model, Authentication authentication) {
        populateHeader(model, authentication);
        model.addAttribute("reportRequest", reportRequest);

        try {
            Map<String, Object> reportData = reportService.generateTransactionReport(
                    reportRequest.getAcctId(),
                    reportRequest.getCardNum(),
                    reportRequest.getStartDate(),
                    reportRequest.getEndDate()
            );
            model.addAttribute("reportData", reportData);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "reports";
    }

    private void populateHeader(Model model, Authentication authentication) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", "CORPT00C");
        model.addAttribute("userId", authentication.getName());
    }
}
