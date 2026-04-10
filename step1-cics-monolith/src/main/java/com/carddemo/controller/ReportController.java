package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mirrors CORPT00C.cbl - Transaction Reports.
 */
@Controller
public class ReportController {

    private static final String PGM_NAME = "CORPT00C";
    private static final String TRAN_ID = "CR00";

    @GetMapping("/report")
    public String showReport(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("reportGenerated", false);
        return "report";
    }

    @PostMapping("/report")
    public String processReport(@RequestParam(defaultValue = "") String action,
                                @RequestParam(defaultValue = "") String reportType,
                                Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) return "redirect:/menu";

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "Report generation submitted. Check batch job status.");
        model.addAttribute("reportGenerated", true);
        return "report";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Transaction Reports");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
