package com.carddemo.usermanagement.service;

import com.carddemo.usermanagement.dto.CreateUserRequest;
import com.carddemo.usermanagement.dto.UpdateUserRequest;
import com.carddemo.usermanagement.dto.UserResponse;
import com.carddemo.usermanagement.entity.User;
import com.carddemo.usermanagement.entity.UserType;
import com.carddemo.usermanagement.exception.DuplicateUserException;
import com.carddemo.usermanagement.exception.UserNotFoundException;
import com.carddemo.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("USER0001", "John", "Smith", "PASS0001", UserType.REGULAR);
    }

    @Nested
    @DisplayName("listUsers — replaces COUSR00C")
    class ListUsersTests {

        @Test
        @DisplayName("returns paginated users sorted by userId")
        void returnsPaginatedUsers() {
            User user2 = new User("USER0002", "Jane", "Doe", "PASS0002", UserType.REGULAR);
            Page<User> page = new PageImpl<>(List.of(sampleUser, user2));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<UserResponse> result = userService.listUsers(0, 10);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo("USER0001");
            assertThat(result.getContent().get(1).getUserId()).isEqualTo("USER0002");
        }

        @Test
        @DisplayName("uses default page size of 10 when size is zero or negative")
        void usesDefaultPageSize() {
            Page<User> page = new PageImpl<>(List.of());
            when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

            userService.listUsers(0, 0);

            verify(userRepository).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getUser — READ operation from COUSR02C/COUSR03C")
    class GetUserTests {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));

            UserResponse response = userService.getUser("USER0001");

            assertThat(response.getUserId()).isEqualTo("USER0001");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getUserType()).isEqualTo("Regular");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user ID not found — mirrors DFHRESP(NOTFND)")
        void throwsWhenNotFound() {
            when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser("UNKNOWN"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User ID NOT found");
        }
    }

    @Nested
    @DisplayName("createUser — replaces COUSR01C")
    class CreateUserTests {

        @Test
        @DisplayName("creates a new Regular user successfully")
        void createsRegularUser() {
            CreateUserRequest request = new CreateUserRequest(
                    "NEWUSR01", "Alice", "Wonder", "PASS1234", "R");
            when(userRepository.existsById("NEWUSR01")).thenReturn(false);
            User savedUser = new User("NEWUSR01", "Alice", "Wonder", "PASS1234", UserType.REGULAR);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponse response = userService.createUser(request);

            assertThat(response.getUserId()).isEqualTo("NEWUSR01");
            assertThat(response.getUserType()).isEqualTo("Regular");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("creates a new Admin user successfully")
        void createsAdminUser() {
            CreateUserRequest request = new CreateUserRequest(
                    "ADMIN02", "Bob", "Builder", "ADM12345", "A");
            when(userRepository.existsById("ADMIN02")).thenReturn(false);
            User savedUser = new User("ADMIN02", "Bob", "Builder", "ADM12345", UserType.ADMIN);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponse response = userService.createUser(request);

            assertThat(response.getUserType()).isEqualTo("Admin");
        }

        @Test
        @DisplayName("throws DuplicateUserException — mirrors DFHRESP(DUPREC)")
        void throwsOnDuplicateUserId() {
            CreateUserRequest request = new CreateUserRequest(
                    "USER0001", "John", "Smith", "PASS0001", "R");
            when(userRepository.existsById("USER0001")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateUserException.class)
                    .hasMessageContaining("User ID already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws on invalid user type code")
        void throwsOnInvalidUserType() {
            CreateUserRequest request = new CreateUserRequest(
                    "NEWUSR02", "Charlie", "Choc", "PASS5678", "X");
            when(userRepository.existsById("NEWUSR02")).thenReturn(false);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid user type code");
        }
    }

    @Nested
    @DisplayName("updateUser — replaces COUSR02C")
    class UpdateUserTests {

        @Test
        @DisplayName("updates user fields that have changed")
        void updatesChangedFields() {
            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
            UpdateUserRequest request = new UpdateUserRequest("Johnny", "Smith", "NEWPASS1", "R");
            User updatedUser = new User("USER0001", "Johnny", "Smith", "NEWPASS1", UserType.REGULAR);
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            UserResponse response = userService.updateUser("USER0001", request);

            assertThat(response.getFirstName()).isEqualTo("Johnny");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("returns unchanged user when no fields differ")
        void returnsUnchangedWhenNoModifications() {
            when(userRepository.findById("USER0001")).thenReturn(Optional.of(sampleUser));
            UpdateUserRequest request = new UpdateUserRequest("John", "Smith", "PASS0001", "R");

            UserResponse response = userService.updateUser("USER0001", request);

            assertThat(response.getFirstName()).isEqualTo("John");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws UserNotFoundException when user ID not found")
        void throwsWhenUserNotFound() {
            when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());
            UpdateUserRequest request = new UpdateUserRequest("Test", "User", "TESTPASS", "R");

            assertThatThrownBy(() -> userService.updateUser("UNKNOWN", request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser — replaces COUSR03C")
    class DeleteUserTests {

        @Test
        @DisplayName("deletes existing user successfully")
        void deletesExistingUser() {
            when(userRepository.existsById("USER0001")).thenReturn(true);

            userService.deleteUser("USER0001");

            verify(userRepository).deleteById("USER0001");
        }

        @Test
        @DisplayName("throws UserNotFoundException when user ID not found")
        void throwsWhenUserNotFound() {
            when(userRepository.existsById("UNKNOWN")).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser("UNKNOWN"))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
        }
    }
}
