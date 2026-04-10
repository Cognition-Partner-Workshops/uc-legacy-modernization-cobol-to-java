package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.menu.MainMenuOptions;
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
 * Mirrors COMEN01C.cbl - Main Menu for Regular users.
 */
@Controller
public class MainMenuController {

    private static final String PGM_NAME = "COMEN01C";
    private static final String TRAN_ID = "CM00";

    private final CicsNavigationService navigationService;

    public MainMenuController(CicsNavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @GetMapping("/menu")
    public String showMenu(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);

        if (commarea == null) {
            return "redirect:/signon";
        }

        // Set PGM-REENTER for next interaction
        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("menuOptions", MainMenuOptions.OPTIONS);
        model.addAttribute("errorMessage", "");
        return "main-menu";
    }

    @PostMapping("/menu")
    public String processMenu(@RequestParam(defaultValue = "") String option,
                              @RequestParam(defaultValue = "") String action,
                              Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);

        if (commarea == null) {
            return "redirect:/signon";
        }

        // Handle PF3 - return to signon
        if ("PF3".equals(action)) {
            commarea.setToProgram("COSGN00C");
            session.setAttribute("CARDDEMO_COMMAREA", commarea);
            return "redirect:/signon";
        }

        populateHeaderInfo(model);
        model.addAttribute("menuOptions", MainMenuOptions.OPTIONS);

        // Parse option - mirrors PROCESS-ENTER-KEY lines 117-134
        int optNum;
        try {
            optNum = Integer.parseInt(option.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Please enter a valid option number...");
            return "main-menu";
        }

        if (optNum < 1 || optNum > MainMenuOptions.MENU_OPT_COUNT) {
            model.addAttribute("errorMessage", "Please enter a valid option number...");
            return "main-menu";
        }

        MainMenuOptions.MenuOption menuOpt = MainMenuOptions.getOption(optNum);

        // Check admin-only access - mirrors lines 136-143
        if (commarea.isUser() && "A".equals(menuOpt.userType())) {
            model.addAttribute("errorMessage", "No access - Admin Only option... ");
            return "main-menu";
        }

        // Check if program is installed - mirrors lines 147-168
        if (!navigationService.isProgramInstalled(menuOpt.programName())) {
            model.addAttribute("errorMessage",
                "This option " + menuOpt.name().trim() + " is not installed...");
            return "main-menu";
        }

        // XCTL to selected program - mirrors lines 177-187
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
        model.addAttribute("title02", "Main Application");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
