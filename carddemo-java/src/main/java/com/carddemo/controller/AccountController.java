package com.carddemo.controller;

import com.carddemo.dto.AccountForm;
import com.carddemo.entity.Account;
import com.carddemo.entity.CreditCard;
import com.carddemo.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Account Controller - maps to COACTVWC/CAVW + COACTUPC/CAUP
 * Screens from app/bms/COACTVW.bms and app/bms/COACTUP.bms
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public String viewAccount(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            Account account = accountService.viewAccount(id);
            List<CreditCard> cards = accountService.getCardsForAccount(id);
            model.addAttribute("account", account);
            model.addAttribute("cards", cards);
            model.addAttribute("tranName", "CAVW");
            model.addAttribute("pgmName", "COACTVWC");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "account-view";
    }

    @GetMapping("/{id}/edit")
    public String editAccount(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            Account account = accountService.viewAccount(id);
            model.addAttribute("account", account);
            model.addAttribute("accountForm", new AccountForm());
            model.addAttribute("tranName", "CAUP");
            model.addAttribute("pgmName", "COACTUPC");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "account-update";
    }

    @PostMapping("/{id}")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute AccountForm form,
                                RedirectAttributes redirectAttributes) {
        try {
            Account updatedFields = new Account();
            updatedFields.setActiveStatus(form.getActiveStatus());
            updatedFields.setCreditLimit(form.getCreditLimit());
            updatedFields.setCashCreditLimit(form.getCashCreditLimit());
            if (form.getExpirationDate() != null && !form.getExpirationDate().isEmpty()) {
                updatedFields.setExpirationDate(LocalDate.parse(form.getExpirationDate()));
            }
            if (form.getReissueDate() != null && !form.getReissueDate().isEmpty()) {
                updatedFields.setReissueDate(LocalDate.parse(form.getReissueDate()));
            }
            updatedFields.setGroupId(form.getGroupId());

            accountService.updateAccount(id, updatedFields);
            redirectAttributes.addFlashAttribute("infoMessage", "Account updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/" + id;
    }

    @GetMapping
    public String listAccounts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Authentication authentication,
                               Model model) {
        Page<Account> accounts = accountService.listAccounts(page, size);
        model.addAttribute("accounts", accounts);
        model.addAttribute("tranName", "CAVW");
        model.addAttribute("pgmName", "COACTVWC");
        return "account-view";
    }
}
