package com.carddemo.controller;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Replaces CAVW (account view) and CAUP (account update) transactions.
 */
@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String listAccounts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Page<Account> accounts = accountService.getAllAccounts(PageRequest.of(page, size));
        model.addAttribute("accounts", accounts);
        return "account-view";
    }

    @GetMapping("/{acctId}")
    public String viewAccount(@PathVariable String acctId, Model model) {
        Account account = accountService.getAccount(acctId);
        model.addAttribute("account", account);
        return "account-view";
    }

    @GetMapping("/{acctId}/edit")
    public String editAccount(@PathVariable String acctId, Model model) {
        Account account = accountService.getAccount(acctId);
        model.addAttribute("account", account);
        model.addAttribute("updateRequest", new AccountUpdateRequest());
        return "account-update";
    }

    @PostMapping("/{acctId}/edit")
    public String updateAccount(@PathVariable String acctId,
                                @ModelAttribute AccountUpdateRequest updateRequest,
                                RedirectAttributes redirectAttributes) {
        accountService.updateAccount(acctId, updateRequest);
        redirectAttributes.addFlashAttribute("message", "Account updated successfully");
        return "redirect:/accounts/" + acctId;
    }
}
