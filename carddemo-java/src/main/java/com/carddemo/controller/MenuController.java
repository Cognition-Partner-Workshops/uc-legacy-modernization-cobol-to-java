package com.carddemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.dto.MenuResponseDto;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import com.carddemo.service.MenuService;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;
    private final UserRepository userRepository;

    public MenuController(MenuService menuService, UserRepository userRepository) {
        this.menuService = menuService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<MenuResponseDto> getMenu(Authentication authentication) {
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        MenuResponseDto menu = menuService.getMenuForUser(user);
        return ResponseEntity.ok(menu);
    }
}
