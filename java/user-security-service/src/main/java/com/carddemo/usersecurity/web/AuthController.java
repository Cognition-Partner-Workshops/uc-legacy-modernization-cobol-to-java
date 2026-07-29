package com.carddemo.usersecurity.web;

import com.carddemo.usersecurity.dto.SignonRequest;
import com.carddemo.usersecurity.dto.SignonResponse;
import com.carddemo.usersecurity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Signon endpoint, modernizing COSGN00C. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"/signon", "/auth/login"})
    public ResponseEntity<SignonResponse> signon(@Valid @RequestBody SignonRequest request) {
        return ResponseEntity.ok(SignonResponse.from(
                userService.authenticate(request.getUserId(), request.getPassword())));
    }
}
