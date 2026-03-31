package com.carddemo.service;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.exception.CardDemoException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void getAllUsers_returnsList() {
        List<UserSecurity> users = List.of(
                UserSecurity.builder().userId("ADMIN001").userType("A").build(),
                UserSecurity.builder().userId("USER0001").userType("U").build()
        );
        when(userSecurityRepository.findAllByOrderByUserIdAsc()).thenReturn(users);

        List<UserSecurity> result = userManagementService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void getUser_found() {
        UserSecurity user = UserSecurity.builder()
                .userId("ADMIN001").firstName("Admin").lastName("User").userType("A").build();
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        UserSecurity result = userManagementService.getUser("admin001");

        assertEquals("ADMIN001", result.getUserId());
    }

    @Test
    void getUser_notFound_throwsException() {
        when(userSecurityRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(CardDemoException.class, () -> userManagementService.getUser("UNKNOWN"));
    }

    @Test
    void createUser_success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUserId("NEWUSER1");
        request.setPassword("PASSWORD");
        request.setFirstName("New");
        request.setLastName("User");
        request.setUserType("U");

        when(userSecurityRepository.existsById("NEWUSER1")).thenReturn(false);
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(i -> i.getArgument(0));

        UserSecurity result = userManagementService.createUser(request);

        assertEquals("NEWUSER1", result.getUserId());
        assertEquals("U", result.getUserType());
    }

    @Test
    void createUser_alreadyExists_throwsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUserId("ADMIN001");
        request.setPassword("PASSWORD");
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setUserType("A");

        when(userSecurityRepository.existsById("ADMIN001")).thenReturn(true);

        assertThrows(CardDemoException.class, () -> userManagementService.createUser(request));
    }

    @Test
    void updateUser_success() {
        UserSecurity existingUser = UserSecurity.builder()
                .userId("USER0001").password("PASSWORD").firstName("Old").lastName("Name").userType("U").build();

        UserCreateRequest request = new UserCreateRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setUserType("A");

        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(existingUser));
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(i -> i.getArgument(0));

        UserSecurity result = userManagementService.updateUser("USER0001", request);

        assertEquals("New", result.getFirstName());
        assertEquals("A", result.getUserType());
    }

    @Test
    void deleteUser_success() {
        UserSecurity user = UserSecurity.builder().userId("USER0001").build();
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(user));

        userManagementService.deleteUser("USER0001");

        verify(userSecurityRepository).delete(user);
    }
}
