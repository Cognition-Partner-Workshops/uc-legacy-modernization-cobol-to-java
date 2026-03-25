/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.model.UserSecurityRecord;
import com.cardemo.repository.UserSecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Management Service - migrated from COBOL programs:
 *   COUSR00C.cbl (User List)
 *   COUSR01C.cbl (User Add)
 *   COUSR02C.cbl (User Update)
 *   COUSR03C.cbl (User Delete)
 * Handles CRUD operations for user security records.
 * Original: CICS programs with admin access, reads/writes/deletes USRSEC file.
 */
@Service
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);
    private static final int PAGE_SIZE = 10;

    private final UserSecurityRepository userSecurityRepository;

    public UserManagementService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    /**
     * List users with pagination - migrated from COUSR00C STARTBR/READNEXT logic.
     */
    public Page<UserSecurityRecord> listUsers(int page) {
        return userSecurityRepository.findAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by("usrId")));
    }

    /**
     * Get user by ID - migrated from CICS READ on USRSEC.
     */
    public UserSecurityRecord getUser(String userId) {
        Optional<UserSecurityRecord> userOpt = userSecurityRepository.findById(userId.toUpperCase());
        if (userOpt.isEmpty()) {
            throw new UserManagementException("User ID not found...");
        }
        return userOpt.get();
    }

    /**
     * Add new user - migrated from COUSR01C WRITE logic.
     */
    @Transactional
    public UserSecurityRecord addUser(String userId, String firstName, String lastName,
                                      String password, String userType) {
        String upperUserId = userId.toUpperCase();

        if (userSecurityRepository.existsById(upperUserId)) {
            throw new UserManagementException("User ID already exists...");
        }

        validateUserInput(upperUserId, firstName, lastName, password, userType);

        UserSecurityRecord user = new UserSecurityRecord();
        user.setUsrId(upperUserId);
        user.setUsrFirstName(firstName);
        user.setUsrLastName(lastName);
        user.setUsrPassword(password.toUpperCase());
        user.setUsrType(userType.toUpperCase());

        UserSecurityRecord saved = userSecurityRepository.save(user);
        log.info("User {} added successfully", upperUserId);
        return saved;
    }

    /**
     * Update existing user - migrated from COUSR02C REWRITE logic.
     */
    @Transactional
    public UserSecurityRecord updateUser(String userId, String firstName, String lastName,
                                         String password, String userType) {
        String upperUserId = userId.toUpperCase();

        Optional<UserSecurityRecord> existing = userSecurityRepository.findById(upperUserId);
        if (existing.isEmpty()) {
            throw new UserManagementException("User ID not found for update...");
        }

        validateUserInput(upperUserId, firstName, lastName, password, userType);

        UserSecurityRecord user = existing.get();
        user.setUsrFirstName(firstName);
        user.setUsrLastName(lastName);
        user.setUsrPassword(password.toUpperCase());
        user.setUsrType(userType.toUpperCase());

        UserSecurityRecord saved = userSecurityRepository.save(user);
        log.info("User {} updated successfully", upperUserId);
        return saved;
    }

    /**
     * Delete user - migrated from COUSR03C DELETE logic.
     */
    @Transactional
    public void deleteUser(String userId) {
        String upperUserId = userId.toUpperCase();

        if (!userSecurityRepository.existsById(upperUserId)) {
            throw new UserManagementException("User ID not found for deletion...");
        }

        userSecurityRepository.deleteById(upperUserId);
        log.info("User {} deleted successfully", upperUserId);
    }

    private void validateUserInput(String userId, String firstName, String lastName,
                                   String password, String userType) {
        if (userId == null || userId.isBlank()) {
            throw new UserManagementException("User ID is required...");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new UserManagementException("First name is required...");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new UserManagementException("Last name is required...");
        }
        if (password == null || password.isBlank()) {
            throw new UserManagementException("Password is required...");
        }
        if (userType == null || (!userType.equalsIgnoreCase("A") && !userType.equalsIgnoreCase("U"))) {
            throw new UserManagementException("User type must be 'A' (Admin) or 'U' (User)...");
        }
    }

    public static class UserManagementException extends RuntimeException {
        public UserManagementException(String message) {
            super(message);
        }
    }
}
