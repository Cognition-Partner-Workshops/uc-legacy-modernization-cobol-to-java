package com.carddemo.service.online;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserDto;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.exception.ValidationException;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service replacing COBOL programs COUSR00C-COUSR03C.
 *
 * <p>COUSR00C (List Users, CICS txn CU00):
 * - Browses USRSEC file (STARTBR/READNEXT)
 * - Displays paginated user list
 *
 * <p>COUSR01C (Add User, CICS txn CU01):
 * - Validates user ID doesn't exist
 * - Writes new user record to USRSEC
 *
 * <p>COUSR02C (Update User, CICS txn CU02):
 * - Reads USRSEC for update
 * - Rewrites record
 *
 * <p>COUSR03C (Delete User, CICS txn CU03):
 * - Reads USRSEC, confirms deletion
 * - Deletes record
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * List all users - replaces COUSR00C STARTBR/READNEXT loop.
     */
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID - replaces COUSR02C/03C initial READ.
     */
    @Transactional(readOnly = true)
    public UserDto getUser(String usrId) {
        User user = userRepository.findById(usrId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", usrId));
        return mapToDto(user);
    }

    /**
     * Create new user - replaces COUSR01C PROCESS-ENTER-KEY paragraph.
     */
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        String usrId = request.getUsrId().toUpperCase();

        if (userRepository.findById(usrId).isPresent()) {
            throw new ValidationException("User ID already exists: " + usrId);
        }

        User user = new User();
        user.setUsrId(usrId);
        user.setUsrFirstName(request.getUsrFirstName());
        user.setUsrLastName(request.getUsrLastName());
        user.setUsrPwd(passwordEncoder.encode(request.getUsrPwd()));
        user.setUsrType(request.getUsrType());

        userRepository.save(user);
        return mapToDto(user);
    }

    /**
     * Update user - replaces COUSR02C PROCESS-ENTER-KEY paragraph.
     */
    @Transactional
    public UserDto updateUser(String usrId, UserCreateRequest request) {
        User user = userRepository.findById(usrId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", usrId));

        if (request.getUsrFirstName() != null) {
            user.setUsrFirstName(request.getUsrFirstName());
        }
        if (request.getUsrLastName() != null) {
            user.setUsrLastName(request.getUsrLastName());
        }
        if (request.getUsrPwd() != null) {
            user.setUsrPwd(passwordEncoder.encode(request.getUsrPwd()));
        }
        if (request.getUsrType() != null) {
            user.setUsrType(request.getUsrType());
        }

        userRepository.save(user);
        return mapToDto(user);
    }

    /**
     * Delete user - replaces COUSR03C DELETE-USER-RECORD paragraph.
     */
    @Transactional
    public void deleteUser(String usrId) {
        User user = userRepository.findById(usrId.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", usrId));
        userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getUsrId(),
                user.getUsrFirstName(),
                user.getUsrLastName(),
                user.getUsrType()
        );
    }
}
