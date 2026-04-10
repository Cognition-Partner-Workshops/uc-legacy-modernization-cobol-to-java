package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.CicsNavigationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Mirrors COSGN00C.cbl - Signon Screen for CardDemo Application.
 */
@Controller
public class SignonController {

    private static final String PGM_NAME = "COSGN00C";
    private static final String TRAN_ID = "CC00";

    private final UserSecurityRepository userSecurityRepository;
    private final CicsNavigationService navigationService;
    private final SecurityContextRepository securityContextRepository;

    public SignonController(UserSecurityRepository userSecurityRepository,
                           CicsNavigationService navigationService) {
        this.userSecurityRepository = userSecurityRepository;
        this.navigationService = navigationService;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
    }

    @GetMapping("/signon")
    public String showSignonScreen(Model model, HttpSession session) {
        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        // New session - no COMMAREA yet (EIBCALEN = 0)
        session.removeAttribute("CARDDEMO_COMMAREA");
        return "signon";
    }

    @PostMapping("/signon")
    public String processSignon(@RequestParam(defaultValue = "") String userId,
                                @RequestParam(defaultValue = "") String password,
                                Model model, HttpSession session,
                                HttpServletRequest request, HttpServletResponse response) {
        populateHeaderInfo(model);

        // Validate input - mirrors PROCESS-ENTER-KEY
        if (userId.isBlank()) {
            model.addAttribute("errorMessage", "Please enter User ID ...");
            return "signon";
        }
        if (password.isBlank()) {
            model.addAttribute("errorMessage", "Please enter Password ...");
            return "signon";
        }

        String upperUserId = userId.toUpperCase().trim();
        String upperPassword = password.toUpperCase().trim();

        // READ-USER-SEC-FILE - mirrors lines 211-219
        Optional<UserSecurity> userOpt = userSecurityRepository.findById(upperUserId);

        if (userOpt.isEmpty()) {
            // RESP=13 (NOTFND) - mirrors line 247-251
            model.addAttribute("errorMessage", "User not found. Try again ...");
            return "signon";
        }

        UserSecurity user = userOpt.get();

        // Plaintext password comparison - mirrors line 223
        if (user.getSecUsrPwd() == null || !upperPassword.equals(user.getSecUsrPwd().trim())) {
            model.addAttribute("errorMessage", "Wrong Password. Try again ...");
            return "signon";
        }

        // Build COMMAREA - mirrors lines 224-228
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setFromTranId(TRAN_ID);
        commarea.setFromProgram(PGM_NAME);
        commarea.setUserId(upperUserId);
        commarea.setUserType(user.getSecUsrType());
        commarea.setPgmContext(0); // CDEMO-PGM-ENTER

        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        // Set Spring Security authentication and persist to session (Spring Security 6.x)
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("A".equals(user.getSecUsrType()) ? "ROLE_ADMIN" : "ROLE_USER")
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(upperUserId, null, authorities);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        // XCTL based on user type - mirrors lines 230-240
        if ("A".equals(user.getSecUsrType())) {
            return "redirect:/admin";
        } else {
            return "redirect:/menu";
        }
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
