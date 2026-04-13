package com.carddemo.controller;

import com.carddemo.dto.UserForm;
import com.carddemo.entity.UserSecurity;
import com.carddemo.service.UserSecurityService;
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

/**
 * User Controller - maps to COUSR00C-03C/CU00-CU03
 * Screens from app/bms/COUSR00.bms through COUSR03.bms
 */
@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserSecurityService userSecurityService;

    public UserController(UserSecurityService userSecurityService) {
        this.userSecurityService = userSecurityService;
    }

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Authentication authentication,
                            Model model) {
        Page<UserSecurity> users = userSecurityService.listUsers(page, size);
        model.addAttribute("users", users);
        model.addAttribute("tranName", "CU00");
        model.addAttribute("pgmName", "COUSR00C");
        return "user-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("tranName", "CU01");
        model.addAttribute("pgmName", "COUSR01C");
        return "user-add";
    }

    @PostMapping
    public String addUser(@ModelAttribute UserForm form,
                          RedirectAttributes redirectAttributes) {
        try {
            UserSecurity user = new UserSecurity();
            user.setUserId(form.getUserId());
            user.setPassword(form.getPassword());
            user.setUserType(form.getUserType());
            user.setFirstName(form.getFirstName());
            user.setLastName(form.getLastName());

            userSecurityService.addUser(user);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "User added successfully: " + form.getUserId());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/add";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable String id, Model model) {
        try {
            UserSecurity user = userSecurityService.getUser(id);
            model.addAttribute("user", user);
            model.addAttribute("userForm", new UserForm());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tranName", "CU02");
        model.addAttribute("pgmName", "COUSR02C");
        return "user-update";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable String id,
                             @ModelAttribute UserForm form,
                             RedirectAttributes redirectAttributes) {
        try {
            UserSecurity updatedFields = new UserSecurity();
            updatedFields.setPassword(form.getPassword());
            updatedFields.setUserType(form.getUserType());
            updatedFields.setFirstName(form.getFirstName());
            updatedFields.setLastName(form.getLastName());

            userSecurityService.updateUser(id, updatedFields);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "User updated successfully: " + id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable String id,
                             RedirectAttributes redirectAttributes) {
        try {
            userSecurityService.deleteUser(id);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "User deleted successfully: " + id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/delete")
    public String showDeleteConfirmation(@PathVariable String id, Model model) {
        try {
            UserSecurity user = userSecurityService.getUser(id);
            model.addAttribute("user", user);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tranName", "CU03");
        model.addAttribute("pgmName", "COUSR03C");
        return "user-delete";
    }
}
