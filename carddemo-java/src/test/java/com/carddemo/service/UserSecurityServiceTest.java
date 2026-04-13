package com.carddemo.service;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSecurityServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @InjectMocks
    private UserSecurityService userSecurityService;

    private UserSecurity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserSecurity();
        testUser.setUserId("TESTUSER");
        testUser.setPassword("TESTPASS");
        testUser.setUserType("U");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void listUsers_returnsPagedResults() {
        Page<UserSecurity> page = new PageImpl<>(List.of(testUser));
        when(userSecurityRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserSecurity> result = userSecurityService.listUsers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("TESTUSER", result.getContent().get(0).getUserId());
    }

    @Test
    void addUser_newUser_saves() {
        when(userSecurityRepository.existsById("NEWUSER1")).thenReturn(false);
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSecurity newUser = new UserSecurity();
        newUser.setUserId("newuser1");
        newUser.setPassword("newpass1");
        newUser.setUserType("U");

        UserSecurity result = userSecurityService.addUser(newUser);

        assertEquals("NEWUSER1", result.getUserId());
        assertEquals("NEWPASS1", result.getPassword());
        verify(userSecurityRepository).save(any(UserSecurity.class));
    }

    @Test
    void addUser_duplicateUser_throwsException() {
        when(userSecurityRepository.existsById("TESTUSER")).thenReturn(true);

        UserSecurity duplicate = new UserSecurity();
        duplicate.setUserId("testuser");
        duplicate.setPassword("pass");

        assertThrows(IllegalArgumentException.class,
                () -> userSecurityService.addUser(duplicate));
    }

    @Test
    void addUser_emptyUserId_throwsException() {
        UserSecurity user = new UserSecurity();
        user.setUserId("");
        user.setPassword("pass");

        assertThrows(IllegalArgumentException.class,
                () -> userSecurityService.addUser(user));
    }

    @Test
    void addUser_emptyPassword_throwsException() {
        UserSecurity user = new UserSecurity();
        user.setUserId("user1");
        user.setPassword("");

        assertThrows(IllegalArgumentException.class,
                () -> userSecurityService.addUser(user));
    }

    @Test
    void updateUser_existingUser_updatesPassword() {
        when(userSecurityRepository.findById("TESTUSER")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any(UserSecurity.class))).thenReturn(testUser);

        UserSecurity updates = new UserSecurity();
        updates.setPassword("newpass");

        UserSecurity result = userSecurityService.updateUser("testuser", updates);

        assertEquals("NEWPASS", testUser.getPassword());
        verify(userSecurityRepository).save(testUser);
    }

    @Test
    void updateUser_existingUser_updatesType() {
        when(userSecurityRepository.findById("TESTUSER")).thenReturn(Optional.of(testUser));
        when(userSecurityRepository.save(any(UserSecurity.class))).thenReturn(testUser);

        UserSecurity updates = new UserSecurity();
        updates.setUserType("A");

        userSecurityService.updateUser("testuser", updates);

        assertEquals("A", testUser.getUserType());
    }

    @Test
    void updateUser_nonExistentUser_throwsException() {
        when(userSecurityRepository.findById("NOUSER")).thenReturn(Optional.empty());

        UserSecurity updates = new UserSecurity();
        updates.setPassword("pass");

        assertThrows(IllegalArgumentException.class,
                () -> userSecurityService.updateUser("nouser", updates));
    }

    @Test
    void deleteUser_existingUser_deletes() {
        when(userSecurityRepository.existsById("TESTUSER")).thenReturn(true);

        userSecurityService.deleteUser("testuser");

        verify(userSecurityRepository).deleteById("TESTUSER");
    }

    @Test
    void deleteUser_nonExistentUser_throwsException() {
        when(userSecurityRepository.existsById("NOUSER")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userSecurityService.deleteUser("nouser"));
    }

    @Test
    void addUser_defaultsToUserType() {
        when(userSecurityRepository.existsById("NEWUSER2")).thenReturn(false);
        when(userSecurityRepository.save(any(UserSecurity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSecurity newUser = new UserSecurity();
        newUser.setUserId("newuser2");
        newUser.setPassword("pass");

        UserSecurity result = userSecurityService.addUser(newUser);

        assertEquals("U", result.getUserType());
    }
}
