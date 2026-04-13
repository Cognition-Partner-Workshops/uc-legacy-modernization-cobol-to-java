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
import java.util.List;

/**
 * Mirrors COUSR00C.cbl - User List.
 */
@Controller
public class UserListController {

    private static final String PGM_NAME = "COUSR00C";
    private static final String TRAN_ID = "CU00";

    private final UserSecurityRepository userSecurityRepository;

    public UserListController(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @GetMapping("/admin/user/list")
    public String showUserList(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        List<UserSecurity> users = userSecurityRepository.findAllByOrderBySecUsrIdAsc();
        model.addAttribute("users", users);
        model.addAttribute("errorMessage", "");
        return "user-list";
    }

    @PostMapping("/admin/user/list")
    public String processUserList(@RequestParam(defaultValue = "") String action,
                                  @RequestParam(defaultValue = "") String selectedUser,
                                  Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        if ("PF3".equals(action)) return "redirect:/admin";

        if (!selectedUser.isBlank()) {
            session.setAttribute("SELECTED_USER_ID", selectedUser.trim());
            return "redirect:/admin/user/update";
        }

        return "redirect:/admin/user/list";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "User List");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
