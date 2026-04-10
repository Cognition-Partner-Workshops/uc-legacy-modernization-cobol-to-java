package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.menu.AdminMenuOptions;
import com.carddemo.service.CicsNavigationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mirrors COADM01C.cbl - Admin Menu.
 */
@Controller
public class AdminMenuController {

    private static final String PGM_NAME = "COADM01C";
    private static final String TRAN_ID = "CA00";

    private final CicsNavigationService navigationService;

    public AdminMenuController(CicsNavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @GetMapping("/admin")
    public String showAdminMenu(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) {
            return "redirect:/signon";
        }

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("adminOptions", AdminMenuOptions.OPTIONS);
        model.addAttribute("errorMessage", "");
        return "admin-menu";
    }

    @PostMapping("/admin")
    public String processAdminMenu(@RequestParam(defaultValue = "") String option,
                                   @RequestParam(defaultValue = "") String action,
                                   Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) {
            return "redirect:/signon";
        }

        if ("PF3".equals(action)) {
            return "redirect:/menu";
        }

        populateHeaderInfo(model);
        model.addAttribute("adminOptions", AdminMenuOptions.OPTIONS);

        int optNum;
        try {
            optNum = Integer.parseInt(option.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Please enter a valid option number...");
            return "admin-menu";
        }

        if (optNum < 1 || optNum > AdminMenuOptions.ADMIN_OPT_COUNT) {
            model.addAttribute("errorMessage", "Please enter a valid option number...");
            return "admin-menu";
        }

        AdminMenuOptions.MenuOption menuOpt = AdminMenuOptions.getOption(optNum);

        if (!navigationService.isProgramInstalled(menuOpt.programName())) {
            model.addAttribute("errorMessage",
                "This option " + menuOpt.name().trim() + " is not installed...");
            return "admin-menu";
        }

        commarea.setFromTranId(TRAN_ID);
        commarea.setFromProgram(PGM_NAME);
        commarea.setPgmContext(0);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        return "redirect:" + navigationService.getUrlForProgram(menuOpt.programName());
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Admin Application");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
