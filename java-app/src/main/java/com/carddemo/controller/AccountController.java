package com.carddemo.controller;

import com.carddemo.dto.AccountSearchRequest;
import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.service.AccountService;
import com.carddemo.util.DateTimeUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/view")
    public String showAccountView(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COACTVWC");
        model.addAttribute("searchRequest", new AccountSearchRequest());
        return "account-view";
    }

    @PostMapping("/view")
    public String viewAccount(@ModelAttribute AccountSearchRequest searchRequest,
                              Model model, Authentication authentication) {
        populateHeader(model, authentication, "COACTVWC");

        if (!accountService.validateAccountId(searchRequest.getAcctId())) {
            model.addAttribute("errorMessage", "Invalid Account ID. Must be a non-zero numeric value up to 11 digits.");
            model.addAttribute("searchRequest", searchRequest);
            return "account-view";
        }

        try {
            Map<String, Object> details = accountService.getAccountDetails(searchRequest.getAcctId());
            model.addAttribute("account", details.get("account"));
            model.addAttribute("customer", details.get("customer"));
            model.addAttribute("cardXrefs", details.get("cardXrefs"));
            model.addAttribute("searchRequest", searchRequest);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("searchRequest", searchRequest);
        }

        return "account-view";
    }

    @GetMapping("/update")
    public String showAccountUpdate(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COACTUPC");
        model.addAttribute("searchRequest", new AccountSearchRequest());
        return "account-update";
    }

    @PostMapping("/update/search")
    public String searchAccountForUpdate(@ModelAttribute AccountSearchRequest searchRequest,
                                         Model model, Authentication authentication) {
        populateHeader(model, authentication, "COACTUPC");

        try {
            Map<String, Object> details = accountService.getAccountDetails(searchRequest.getAcctId());
            model.addAttribute("account", details.get("account"));
            model.addAttribute("customer", details.get("customer"));
            model.addAttribute("searchRequest", searchRequest);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("searchRequest", searchRequest);
        }

        return "account-update";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute AccountUpdateRequest updateRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.updateAccount(updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/update";
    }

    private void populateHeader(Model model, Authentication authentication, String programName) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", programName);
        model.addAttribute("userId", authentication.getName());
    }
}
