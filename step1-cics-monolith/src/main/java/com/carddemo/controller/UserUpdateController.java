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
 * Mirrors COUSR02C.cbl - User Update.
 */
@Controller
public class UserUpdateController {

    private static final String PGM_NAME = "COUSR02C";
    private static final String TRAN_ID = "CU02";

    private final UserSecurityRepository userSecurityRepository;

    public UserUpdateController(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @GetMapping("/admin/user/update")
    public String showUserUpdate(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("user", null);

        String selectedUserId = (String) session.getAttribute("SELECTED_USER_ID");
        if (selectedUserId != null && !selectedUserId.isBlank()) {
            userSecurityRepository.findById(selectedUserId)
                .ifPresent(u -> model.addAttribute("user", u));
        }

        return "user-update";
    }

    @PostMapping("/admin/user/update")
    public String processUserUpdate(@RequestParam(defaultValue = "") String userId,
                                    @RequestParam(defaultValue = "") String firstName,
                                    @RequestParam(defaultValue = "") String lastName,
                                    @RequestParam(defaultValue = "") String password,
                                    @RequestParam(defaultValue = "") String userType,
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
            return "user-update";
        }

        Optional<UserSecurity> userOpt = userSecurityRepository.findById(userId.trim().toUpperCase());
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User not found...");
            model.addAttribute("user", null);
            return "user-update";
        }

        UserSecurity user = userOpt.get();

        if ("Y".equalsIgnoreCase(confirm)) {
            if (!firstName.isBlank()) user.setSecUsrFname(firstName.trim());
            if (!lastName.isBlank()) user.setSecUsrLname(lastName.trim());
            if (!password.isBlank()) user.setSecUsrPwd(password.toUpperCase().trim());
            if (!userType.isBlank()) user.setSecUsrType(userType.toUpperCase().trim());
            userSecurityRepository.save(user);
            model.addAttribute("errorMessage", "User updated successfully...");
        } else {
            model.addAttribute("errorMessage", "Confirm update (Y/N)...");
        }

        model.addAttribute("user", user);
        return "user-update";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "User Update");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
