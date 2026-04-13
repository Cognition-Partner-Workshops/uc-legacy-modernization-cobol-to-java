package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Mirrors COUSR03C.cbl - User Delete.
 */
@Controller
public class UserDeleteController {

    private static final String PGM_NAME = "COUSR03C";
    private static final String TRAN_ID = "CU03";

    private final UserSecurityRepository userSecurityRepository;

    public UserDeleteController(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @GetMapping("/admin/user/delete")
    public String showUserDelete(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("user", null);
        return "user-delete";
    }

    @PostMapping("/admin/user/delete")
    public String processUserDelete(@RequestParam(defaultValue = "") String userId,
                                    @RequestParam(defaultValue = "") String confirm,
                                    @RequestParam(defaultValue = "") String action,
                                    Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        if ("PF3".equals(action)) return "redirect:/admin/user/list";

        populateHeaderInfo(model);

        if (userId.isBlank()) {
            model.addAttribute("errorMessage", "User ID is required...");
            model.addAttribute("user", null);
            return "user-delete";
        }

        Optional<UserSecurity> userOpt = userSecurityRepository.findById(userId.trim().toUpperCase());
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User not found...");
            model.addAttribute("user", null);
            return "user-delete";
        }

        UserSecurity user = userOpt.get();
        model.addAttribute("user", user);

        if ("Y".equalsIgnoreCase(confirm)) {
            userSecurityRepository.delete(user);
            model.addAttribute("errorMessage", "User deleted successfully...");
            model.addAttribute("user", null);
        } else {
            model.addAttribute("errorMessage", "Confirm delete (Y/N)...");
        }

        return "user-delete";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "User Delete");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
