package com.carddemo.usersecurity.service;

import com.carddemo.usersecurity.domain.User;
import com.carddemo.usersecurity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic of the CardDemo user-security module. Each method mirrors one
 * of the legacy CICS programs that operated on the USRSEC file.
 */
@Service
@Transactional
public class UserService {

    /** COUSR00C displays ten users per screen. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** COSGN00C: read USRSEC by user ID, then compare SEC-USR-PWD. */
    @Transactional(readOnly = true)
    public User authenticate(String userId, String password) {
        User user = getById(userId);
        if (!user.getPassword().equals(normalize(password))) {
            throw new WrongPasswordException();
        }
        return user;
    }

    /** COUSR00C: paginated browse of USRSEC ordered by user ID. */
    @Transactional(readOnly = true)
    public Page<User> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0),
                size > 0 ? size : DEFAULT_PAGE_SIZE,
                Sort.by("userId").ascending());
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getById(String userId) {
        String key = normalize(userId);
        return userRepository.findById(key).orElseThrow(() -> new UserNotFoundException(key));
    }

    /** COUSR01C: WRITE to USRSEC, rejecting duplicate keys. */
    public User create(User user) {
        String key = normalize(user.getUserId());
        if (userRepository.existsById(key)) {
            throw new UserAlreadyExistsException(key);
        }
        user.setUserId(key);
        user.setPassword(normalize(user.getPassword()));
        user.setUserType(normalize(user.getUserType()));
        return userRepository.save(user);
    }

    /** COUSR02C: READ FOR UPDATE then REWRITE first name, last name, password and type. */
    public User update(String userId, String firstName, String lastName, String password, String userType) {
        User user = getById(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(normalize(password));
        user.setUserType(normalize(userType));
        return userRepository.save(user);
    }

    /** COUSR03C: READ then DELETE from USRSEC. */
    public void delete(String userId) {
        userRepository.delete(getById(userId));
    }

    /**
     * The BMS maps feed uppercase, space padded fields to the COBOL programs; trim
     * and upper-case so REST callers get the same lookup semantics.
     */
    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
