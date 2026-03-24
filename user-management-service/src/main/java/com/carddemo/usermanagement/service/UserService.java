package com.carddemo.usermanagement.service;

import com.carddemo.usermanagement.dto.CreateUserRequest;
import com.carddemo.usermanagement.dto.UpdateUserRequest;
import com.carddemo.usermanagement.dto.UserResponse;
import com.carddemo.usermanagement.entity.User;
import com.carddemo.usermanagement.entity.UserType;
import com.carddemo.usermanagement.exception.DuplicateUserException;
import com.carddemo.usermanagement.exception.UserNotFoundException;
import com.carddemo.usermanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer implementing the business logic extracted from the four COBOL programs:
 *
 * <ul>
 *   <li><b>COUSR00C</b> — List users with paginated browsing (STARTBR/READNEXT/READPREV)</li>
 *   <li><b>COUSR01C</b> — Add a new user (validate fields, check for duplicate, WRITE)</li>
 *   <li><b>COUSR02C</b> — Update an existing user (READ for update, validate, REWRITE)</li>
 *   <li><b>COUSR03C</b> — Delete a user (READ for update, DELETE)</li>
 * </ul>
 *
 * <p>The original COBOL programs browse the USRSEC VSAM file with a page size of 10 records.
 * This service uses Spring Data's {@link Pageable} for equivalent functionality.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * List users with pagination, sorted by userId ascending.
     * Replaces COUSR00C's sequential browse with STARTBR/READNEXT (forward)
     * and READPREV (backward) logic. The COBOL program displays 10 records per page.
     *
     * @param page zero-based page number
     * @param size number of records per page (defaults to 10 to match COBOL page size)
     * @return a page of user responses
     */
    public Page<UserResponse> listUsers(int page, int size) {
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").ascending());
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    /**
     * Retrieve a single user by ID.
     * Mirrors the READ operation in COUSR02C and COUSR03C.
     *
     * @param userId the user ID to look up
     * @return the user response
     * @throws UserNotFoundException if the user ID is not found (DFHRESP(NOTFND))
     */
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.fromEntity(user);
    }

    /**
     * Create a new user. Replicates the COUSR01C logic:
     * <ol>
     *   <li>Validate all fields are non-empty (done via DTO annotations)</li>
     *   <li>Check if user ID already exists (COUSR01C does READ before WRITE)</li>
     *   <li>Write the new record to USRSEC</li>
     * </ol>
     *
     * @param request the create user request DTO
     * @return the created user response with message "User {id} has been added ..."
     * @throws DuplicateUserException if the user ID already exists (DFHRESP(DUPREC))
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsById(request.getUserId())) {
            throw new DuplicateUserException(request.getUserId());
        }

        UserType userType = UserType.fromCode(request.getUserType());

        User user = new User(
                request.getUserId(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                userType
        );

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Update an existing user. Replicates the COUSR02C logic:
     * <ol>
     *   <li>Read the existing record by ID (with UPDATE intent)</li>
     *   <li>Validate all fields are non-empty (done via DTO annotations)</li>
     *   <li>Compare each field — only rewrite if at least one field changed</li>
     *   <li>REWRITE the updated record</li>
     * </ol>
     *
     * @param userId  the user ID from the path
     * @param request the update user request DTO
     * @return the updated user response with message "User {id} has been updated ..."
     * @throws UserNotFoundException if the user ID is not found (DFHRESP(NOTFND))
     */
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserType newType = UserType.fromCode(request.getUserType());

        boolean modified = false;
        if (!request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            modified = true;
        }
        if (!request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            modified = true;
        }
        if (!request.getPassword().equals(user.getPassword())) {
            user.setPassword(request.getPassword());
            modified = true;
        }
        if (newType != user.getUserType()) {
            user.setUserType(newType);
            modified = true;
        }

        if (!modified) {
            return UserResponse.fromEntity(user);
        }

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Delete a user. Replicates the COUSR03C logic:
     * <ol>
     *   <li>Read the existing record by ID (with UPDATE intent)</li>
     *   <li>DELETE the record</li>
     * </ol>
     *
     * @param userId the user ID to delete
     * @throws UserNotFoundException if the user ID is not found (DFHRESP(NOTFND))
     */
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
}
