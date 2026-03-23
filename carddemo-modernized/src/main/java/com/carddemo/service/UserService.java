package com.carddemo.service;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.dto.UserRequest;
import com.carddemo.exception.AuthenticationException;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repository.cassandra.UserSecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * User service - modernized business logic from COBOL programs:
 *
 *   COSGN00C.cbl  -> authenticate()    (Sign-on - CC00 transaction)
 *   COUSR00C.cbl  -> listUsers()       (List Users - CU00 transaction)
 *   COUSR01C.cbl  -> createUser()      (Add User - CU01 transaction)
 *   COUSR02C.cbl  -> updateUser()      (Update User - CU02 transaction)
 *   COUSR03C.cbl  -> deleteUser()      (Delete User - CU03 transaction)
 *
 * Legacy authentication flow (COSGN00C):
 *   1. Receive USERIDI and PASSWDI from BMS map COSGN0A
 *   2. UPPER-CASE the user ID and password
 *   3. EXEC CICS READ DATASET('USRSEC') INTO(SEC-USER-DATA) RIDFLD(WS-USER-ID)
 *   4. Compare SEC-USR-PWD with input password
 *   5. On match: populate CARDDEMO-COMMAREA with user info
 *      - If CDEMO-USRTYP-ADMIN -> XCTL to COADM01C (Admin Menu)
 *      - Else -> XCTL to COMEN01C (Main Menu)
 *   6. On RESP 13 (NOTFND): "User not found. Try again ..."
 *   7. On password mismatch: "Wrong Password. Try again ..."
 *
 * Modernized: Reactive authentication with token-based response
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserSecurityRepository userRepository;

    public UserService(UserSecurityRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<LoginResponse> authenticate(LoginRequest request) {
        String userId = request.getUserId().toUpperCase();
        String password = request.getPassword().toUpperCase();

        log.debug("Authentication attempt for user: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AuthenticationException(
                        "User not found. Try again ...")))
                .flatMap(user -> {
                    if (!user.getPassword().equals(password)) {
                        return Mono.error(new AuthenticationException(
                                "Wrong Password. Try again ..."));
                    }

                    LoginResponse response = new LoginResponse();
                    response.setUserId(user.getUserId());
                    response.setUserType(user.getUserType());
                    response.setFirstName(user.getFirstName());
                    response.setLastName(user.getLastName());
                    response.setToken("session-" + userId + "-" + System.currentTimeMillis());
                    response.setMessage(user.isAdmin() ? "Admin login successful" : "User login successful");

                    log.info("User {} authenticated successfully (type: {})",
                            userId, user.getUserType());
                    return Mono.just(response);
                });
    }

    public Flux<UserSecurity> listUsers() {
        return userRepository.findAll();
    }

    public Mono<UserSecurity> getUserById(String userId) {
        return userRepository.findById(userId.toUpperCase())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "User not found: " + userId)));
    }

    public Mono<UserSecurity> createUser(UserRequest request) {
        String userId = request.getUserId().toUpperCase();
        log.debug("Creating user: {}", userId);

        return userRepository.findById(userId)
                .flatMap(existing -> Mono.<UserSecurity>error(
                        new BusinessValidationException("User already exists: " + userId)))
                .switchIfEmpty(Mono.defer(() -> {
                    UserSecurity user = new UserSecurity();
                    user.setUserId(userId);
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setPassword(request.getPassword().toUpperCase());
                    user.setUserType(request.getUserType().toUpperCase());
                    return userRepository.save(user);
                }));
    }

    public Mono<UserSecurity> updateUser(String userId, UserRequest request) {
        log.debug("Updating user: {}", userId);
        return userRepository.findById(userId.toUpperCase())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "User not found: " + userId)))
                .flatMap(existing -> {
                    if (request.getFirstName() != null) {
                        existing.setFirstName(request.getFirstName());
                    }
                    if (request.getLastName() != null) {
                        existing.setLastName(request.getLastName());
                    }
                    if (request.getPassword() != null) {
                        existing.setPassword(request.getPassword().toUpperCase());
                    }
                    if (request.getUserType() != null) {
                        existing.setUserType(request.getUserType().toUpperCase());
                    }
                    return userRepository.save(existing);
                });
    }

    public Mono<Void> deleteUser(String userId) {
        log.debug("Deleting user: {}", userId);
        return userRepository.findById(userId.toUpperCase())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "User not found: " + userId)))
                .flatMap(user -> userRepository.deleteById(userId.toUpperCase()));
    }
}
