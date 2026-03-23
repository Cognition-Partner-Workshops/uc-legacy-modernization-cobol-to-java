package com.carddemo.controller;

import com.carddemo.dto.UserRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.service.UserManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Replaces CU00 (user list), CU01 (user add), CU02 (user update), CU03 (user delete).
 */
@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<UserSecurity> users = userManagementService.getAllUsers();
        model.addAttribute("users", users);
        return "user-list";
    }

    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        return "user-add";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute UserRequest request,
                          RedirectAttributes redirectAttributes) {
        try {
            userManagementService.addUser(request);
            redirectAttributes.addFlashAttribute("message", "User added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/add";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{userId}/edit")
    public String editUserForm(@PathVariable String userId, Model model) {
        UserSecurity user = userManagementService.getUser(userId);
        model.addAttribute("user", user);
        UserRequest request = new UserRequest();
        request.setUserId(user.getUserId());
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setUserType(user.getUserType());
        model.addAttribute("userRequest", request);
        return "user-update";
    }

    @PostMapping("/{userId}/edit")
    public String updateUser(@PathVariable String userId,
                             @ModelAttribute UserRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateUser(userId, request);
            redirectAttributes.addFlashAttribute("message", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{userId}/delete")
    public String deleteUserForm(@PathVariable String userId, Model model) {
        UserSecurity user = userManagementService.getUser(userId);
        model.addAttribute("user", user);
        return "user-delete";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable String userId,
                             RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
