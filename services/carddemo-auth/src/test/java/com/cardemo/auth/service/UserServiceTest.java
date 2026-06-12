package com.cardemo.auth.service;

import com.cardemo.auth.dto.CreateUserRequest;
import com.cardemo.auth.dto.UpdateUserRequest;
import com.cardemo.auth.dto.UserResponse;
import com.cardemo.auth.entity.User;
import com.cardemo.auth.entity.User.UserType;
import com.cardemo.auth.repository.UserRepository;
import com.cardemo.auth.service.UserService.UserAlreadyExistsException;
import com.cardemo.auth.service.UserService.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        List<User> users = List.of(
                new User("ADMIN01", "John", "Doe", "enc", UserType.ADMIN),
                new User("USER01", "Jane", "Smith", "enc", UserType.USER)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("ADMIN01", result.get(0).userId());
        assertEquals("USER01", result.get(1).userId());
    }

    @Test
    void getUserById_existingUser_returnsUser() {
        User user = new User("USER01", "Jane", "Smith", "enc", UserType.USER);
        when(userRepository.findByUserId("USER01")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById("USER01");

        assertEquals("USER01", result.userId());
        assertEquals("Jane", result.firstName());
    }

    @Test
    void getUserById_nonExistingUser_throwsException() {
        when(userRepository.findByUserId("NOUSER")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById("NOUSER"));
    }

    @Test
    void createUser_newUser_createsSuccessfully() {
        when(userRepository.existsByUserId("NEWUSR")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateUserRequest request = new CreateUserRequest("NEWUSR", "New", "User", "pass123", "USER");
        UserResponse result = userService.createUser(request);

        assertEquals("NEWUSR", result.userId());
        assertEquals("New", result.firstName());
        assertEquals("USER", result.userType());
    }

    @Test
    void createUser_existingUser_throwsException() {
        when(userRepository.existsByUserId("EXIST")).thenReturn(true);

        CreateUserRequest request = new CreateUserRequest("EXIST", "A", "B", "pass", "USER");
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    void updateUser_existingUser_updatesFields() {
        User user = new User("USER01", "Jane", "Smith", "enc", UserType.USER);
        when(userRepository.findByUserId("USER01")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("Janet", null, null, null);
        UserResponse result = userService.updateUser("USER01", request);

        assertEquals("Janet", result.firstName());
        assertEquals("Smith", result.lastName());
    }

    @Test
    void deleteUser_existingUser_deletesSuccessfully() {
        when(userRepository.existsByUserId("USER01")).thenReturn(true);

        userService.deleteUser("USER01");

        verify(userRepository).deleteById("USER01");
    }

    @Test
    void deleteUser_nonExistingUser_throwsException() {
        when(userRepository.existsByUserId("NOUSER")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("NOUSER"));
    }
}
