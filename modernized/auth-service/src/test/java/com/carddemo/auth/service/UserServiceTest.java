package com.carddemo.auth.service;

import com.carddemo.auth.dto.CreateUserRequest;
import com.carddemo.auth.dto.UpdateUserRequest;
import com.carddemo.auth.dto.UserDto;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void listUsers_shouldReturnAllUsers() {
        User user = User.builder().userId("USER0001").firstName("John").lastName("Doe").userType("U").build();
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.listUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("USER0001");
    }

    @Test
    void getUser_shouldReturnUserDto() {
        User user = User.builder().userId("USER0001").firstName("John").lastName("Doe").userType("U").build();
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));

        UserDto result = userService.getUser("USER0001");

        assertThat(result.getUserId()).isEqualTo("USER0001");
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void getUser_notFound_shouldThrow() {
        when(userRepository.findById("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_shouldHashPassword() {
        when(userRepository.existsById("NEW001")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateUserRequest request = new CreateUserRequest("NEW001", "Jane", "Smith", "secret", "U");
        UserDto result = userService.createUser(request);

        assertThat(result.getUserId()).isEqualTo("NEW001");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(passwordEncoder.matches("secret", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void createUser_duplicate_shouldThrow() {
        when(userRepository.existsById("DUP001")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new CreateUserRequest("DUP001", "A", "B", "pass", "U")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateUser_shouldUpdateFields() {
        User user = User.builder().userId("USER0001").firstName("John").lastName("Doe")
                .passwordHash("old").userType("U").build();
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        UserDto result = userService.updateUser("USER0001", request);

        assertThat(result.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void deleteUser_shouldCallRepository() {
        when(userRepository.existsById("USER0001")).thenReturn(true);

        userService.deleteUser("USER0001");

        verify(userRepository).deleteById("USER0001");
    }

    @Test
    void deleteUser_notFound_shouldThrow() {
        when(userRepository.existsById("MISSING")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
