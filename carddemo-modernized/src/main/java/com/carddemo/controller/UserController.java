package com.carddemo.controller;

import com.carddemo.dto.UserRequest;
import com.carddemo.model.UserSecurity;
import com.carddemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Admin User Management controller - replaces COBOL programs:
 *   COUSR00C.cbl (CU00 transaction) -> GET    /api/admin/users
 *   COUSR01C.cbl (CU01 transaction) -> POST   /api/admin/users
 *   COUSR02C.cbl (CU02 transaction) -> PUT    /api/admin/users/{userId}
 *   COUSR03C.cbl (CU03 transaction) -> DELETE /api/admin/users/{userId}
 *
 * Legacy CICS flow (COUSR00C - List Users):
 *   1. STARTBR DATASET('USRSEC')
 *   2. READNEXT loop to populate user list on BMS map
 *   3. User selects record for Update (U) or Delete (D) action
 *   4. XCTL to COUSR02C or COUSR03C respectively
 *
 * Access: Admin users only (CDEMO-USRTYP-ADMIN / userType = 'A')
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<UserSecurity> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/{userId}")
    public Mono<UserSecurity> getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserSecurity> createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{userId}")
    public Mono<UserSecurity> updateUser(@PathVariable String userId,
                                         @Valid @RequestBody UserRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable String userId) {
        return userService.deleteUser(userId);
    }
}
