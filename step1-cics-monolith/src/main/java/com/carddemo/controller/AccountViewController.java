package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Account;
import com.carddemo.repository.AccountRepository;
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
 * Mirrors COACTVWC.cbl - Account View.
 */
@Controller
public class AccountViewController {

    private static final String PGM_NAME = "COACTVWC";
    private static final String TRAN_ID = "CA01";

    private final AccountRepository accountRepository;

    public AccountViewController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/account/view")
    public String showAccountView(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("account", null);

        if (commarea.getAcctId() > 0) {
            Optional<Account> acct = accountRepository.findById(commarea.getAcctId());
            acct.ifPresent(a -> model.addAttribute("account", a));
        }

        return "account-view";
    }

    @PostMapping("/account/view")
    public String processAccountView(@RequestParam(defaultValue = "") String acctId,
                                     @RequestParam(defaultValue = "") String action,
                                     Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) {
            return "redirect:/menu";
        }

        populateHeaderInfo(model);

        if (acctId.isBlank()) {
            model.addAttribute("errorMessage", "Please enter an Account ID...");
            model.addAttribute("account", null);
            return "account-view";
        }

        try {
            long id = Long.parseLong(acctId.trim());
            commarea.setAcctId(id);
            session.setAttribute("CARDDEMO_COMMAREA", commarea);

            Optional<Account> acct = accountRepository.findById(id);
            if (acct.isPresent()) {
                model.addAttribute("account", acct.get());
                model.addAttribute("errorMessage", "");
            } else {
                model.addAttribute("account", null);
                model.addAttribute("errorMessage", "Account not found...");
            }
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Account ID must be numeric...");
            model.addAttribute("account", null);
        }

        return "account-view";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Account View");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
