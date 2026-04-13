package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mirrors COUSR01C.cbl - User Add.
 */
@Controller
public class UserAddController {

    private static final String PGM_NAME = "COUSR01C";
    private static final String TRAN_ID = "CU01";

    private final UserSecurityRepository userSecurityRepository;

    public UserAddController(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @GetMapping("/admin/user/add")
    public String showUserAdd(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        return "user-add";
    }

    @PostMapping("/admin/user/add")
    public String processUserAdd(@RequestParam(defaultValue = "") String userId,
                                 @RequestParam(defaultValue = "") String firstName,
                                 @RequestParam(defaultValue = "") String lastName,
                                 @RequestParam(defaultValue = "") String password,
                                 @RequestParam(defaultValue = "") String userType,
                                 @RequestParam(defaultValue = "") String action,
                                 Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";
        if (!commarea.isAdmin()) return "redirect:/menu";

        if ("PF3".equals(action)) return "redirect:/admin";

        populateHeaderInfo(model);
        model.addAttribute("userId", userId);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("userType", userType);

        if (userId.isBlank()) {
            model.addAttribute("errorMessage", "User ID is required...");
            return "user-add";
        }
        if (password.isBlank()) {
            model.addAttribute("errorMessage", "Password is required...");
            return "user-add";
        }
        if (userType.isBlank() || (!"A".equalsIgnoreCase(userType) && !"U".equalsIgnoreCase(userType))) {
            model.addAttribute("errorMessage", "User Type must be 'A' (Admin) or 'U' (User)...");
            return "user-add";
        }

        String normalizedUserId = userId.toUpperCase().trim();
        if (userSecurityRepository.existsById(normalizedUserId)) {
            model.addAttribute("errorMessage", "User already exists...");
            return "user-add";
        }

        try {
            UserSecurity user = new UserSecurity();
            user.setSecUsrId(normalizedUserId);
            user.setSecUsrFname(firstName.trim());
            user.setSecUsrLname(lastName.trim());
            user.setSecUsrPwd(password.toUpperCase().trim());
            user.setSecUsrType(userType.toUpperCase().trim());
            userSecurityRepository.save(user);

            model.addAttribute("errorMessage", "User added successfully...");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "User already exists...");
        }

        return "user-add";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "User Add");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
