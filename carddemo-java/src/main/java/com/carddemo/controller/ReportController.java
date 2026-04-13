package com.carddemo.controller;

import com.carddemo.dto.ReportRequestForm;
import com.carddemo.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Report Controller - maps to CORPT00C/CR00
 * Screen from app/bms/CORPT00.bms
 */
@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String showReportForm(Authentication authentication, Model model) {
        model.addAttribute("reportRequestForm", new ReportRequestForm());
        model.addAttribute("tranName", "CR00");
        model.addAttribute("pgmName", "CORPT00C");
        return "report-request";
    }

    @PostMapping
    public String requestReport(@ModelAttribute ReportRequestForm form,
                                RedirectAttributes redirectAttributes) {
        try {
            LocalDate startDate = LocalDate.parse(form.getStartDate());
            LocalDate endDate = LocalDate.parse(form.getEndDate());
            reportService.requestReport(form.getReportType(), startDate, endDate);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Report request submitted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error submitting report request: " + e.getMessage());
        }
        return "redirect:/reports";
    }
}
