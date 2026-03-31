package com.carddemo.controller;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.model.UserSecurity;
import com.carddemo.service.UserManagementService;
import com.carddemo.util.DateTimeUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public String listUsers(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COUSR00C");
        List<UserSecurity> users = userManagementService.getAllUsers();
        model.addAttribute("users", users);
        return "user-list";
    }

    @GetMapping("/add")
    public String showAddUser(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COUSR01C");
        model.addAttribute("userRequest", new UserCreateRequest());
        return "user-add";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("userRequest") UserCreateRequest request,
                           BindingResult result,
                           Model model, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        populateHeader(model, authentication, "COUSR01C");

        if (result.hasErrors()) {
            return "user-add";
        }

        try {
            userManagementService.createUser(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + request.getUserId() + " created successfully.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user-add";
        }
    }

    @GetMapping("/update")
    public String showUpdateUser(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COUSR02C");
        return "user-update";
    }

    @GetMapping("/update/{userId}")
    public String showUpdateUserForm(@PathVariable String userId,
                                      Model model, Authentication authentication) {
        populateHeader(model, authentication, "COUSR02C");
        try {
            UserSecurity user = userManagementService.getUser(userId);
            UserCreateRequest request = new UserCreateRequest();
            request.setUserId(user.getUserId());
            request.setFirstName(user.getFirstName());
            request.setLastName(user.getLastName());
            request.setUserType(user.getUserType());
            model.addAttribute("userRequest", request);
            model.addAttribute("existingUser", user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "user-update";
    }

    @PostMapping("/update/{userId}")
    public String updateUser(@PathVariable String userId,
                              @ModelAttribute("userRequest") UserCreateRequest request,
                              RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateUser(userId, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + userId + " updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete")
    public String showDeleteUser(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COUSR03C");
        List<UserSecurity> users = userManagementService.getAllUsers();
        model.addAttribute("users", users);
        return "user-delete";
    }

    @PostMapping("/delete/{userId}")
    public String deleteUser(@PathVariable String userId,
                              RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + userId + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void populateHeader(Model model, Authentication authentication, String programName) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", programName);
        model.addAttribute("userId", authentication.getName());
    }
}
