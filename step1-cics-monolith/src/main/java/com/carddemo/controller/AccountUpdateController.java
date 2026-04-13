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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Mirrors COACTUPC.cbl - Account Update.
 */
@Controller
public class AccountUpdateController {

    private static final String PGM_NAME = "COACTUPC";
    private static final String TRAN_ID = "CA02";

    private final AccountRepository accountRepository;

    public AccountUpdateController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/account/update")
    public String showAccountUpdate(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("account", null);

        if (commarea.getAcctId() > 0) {
            accountRepository.findById(commarea.getAcctId())
                .ifPresent(a -> model.addAttribute("account", a));
        }

        return "account-update";
    }

    @PostMapping("/account/update")
    public String processAccountUpdate(@RequestParam(defaultValue = "") String acctId,
                                       @RequestParam(defaultValue = "") String action,
                                       @RequestParam(defaultValue = "") String acctActiveStatus,
                                       @RequestParam(defaultValue = "") String acctCreditLimit,
                                       @RequestParam(defaultValue = "") String acctCashCreditLimit,
                                       @RequestParam(defaultValue = "") String confirm,
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
            return "account-update";
        }

        try {
            long id = Long.parseLong(acctId.trim());
            commarea.setAcctId(id);
            session.setAttribute("CARDDEMO_COMMAREA", commarea);

            Optional<Account> acctOpt = accountRepository.findById(id);
            if (acctOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Account not found...");
                model.addAttribute("account", null);
                return "account-update";
            }

            Account account = acctOpt.get();

            if ("Y".equalsIgnoreCase(confirm)) {
                if (!acctActiveStatus.isBlank()) {
                    account.setAcctActiveStatus(acctActiveStatus.trim());
                }
                if (!acctCreditLimit.isBlank()) {
                    try {
                        account.setAcctCreditLimit(new BigDecimal(acctCreditLimit.trim()));
                    } catch (NumberFormatException ex) {
                        model.addAttribute("errorMessage", "Credit Limit must be numeric...");
                        model.addAttribute("account", account);
                        return "account-update";
                    }
                }
                if (!acctCashCreditLimit.isBlank()) {
                    try {
                        account.setAcctCashCreditLimit(new BigDecimal(acctCashCreditLimit.trim()));
                    } catch (NumberFormatException ex) {
                        model.addAttribute("errorMessage", "Cash Credit Limit must be numeric...");
                        model.addAttribute("account", account);
                        return "account-update";
                    }
                }
                accountRepository.save(account);
                model.addAttribute("errorMessage", "Account updated successfully...");
            } else {
                model.addAttribute("errorMessage", "Confirm update (Y/N)...");
            }

            model.addAttribute("account", account);
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Account ID must be numeric...");
            model.addAttribute("account", null);
        }

        return "account-update";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Account Update");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
