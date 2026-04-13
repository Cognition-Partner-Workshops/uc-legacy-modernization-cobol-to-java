package com.carddemo.repository;

import com.carddemo.entity.UserSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserSecurityRepositoryTest {

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @BeforeEach
    void setUp() {
        userSecurityRepository.deleteAll();

        UserSecurity admin = new UserSecurity();
        admin.setUserId("ADMIN001");
        admin.setPassword("ADMIN001");
        admin.setUserType("A");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        userSecurityRepository.save(admin);

        UserSecurity user = new UserSecurity();
        user.setUserId("USER0001");
        user.setPassword("USER0001");
        user.setUserType("U");
        user.setFirstName("Regular");
        user.setLastName("User");
        userSecurityRepository.save(user);
    }

    @Test
    void findById_existingUser_returnsUser() {
        Optional<UserSecurity> found = userSecurityRepository.findById("ADMIN001");

        assertTrue(found.isPresent());
        assertEquals("A", found.get().getUserType());
        assertEquals("Admin", found.get().getFirstName());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        Optional<UserSecurity> found = userSecurityRepository.findById("NOUSER");
        assertFalse(found.isPresent());
    }

    @Test
    void save_createsNewUser() {
        UserSecurity newUser = new UserSecurity();
        newUser.setUserId("NEWUSER1");
        newUser.setPassword("NEWPASS1");
        newUser.setUserType("U");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        userSecurityRepository.save(newUser);

        assertTrue(userSecurityRepository.findById("NEWUSER1").isPresent());
    }

    @Test
    void update_modifiesPassword() {
        UserSecurity user = userSecurityRepository.findById("USER0001").orElseThrow();
        user.setPassword("NEWPASS1");
        userSecurityRepository.save(user);

        UserSecurity updated = userSecurityRepository.findById("USER0001").orElseThrow();
        assertEquals("NEWPASS1", updated.getPassword());
    }

    @Test
    void delete_removesUser() {
        userSecurityRepository.deleteById("USER0001");
        assertFalse(userSecurityRepository.findById("USER0001").isPresent());
    }

    @Test
    void findAll_returnsAllUsers() {
        List<UserSecurity> all = userSecurityRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void existsById_existingUser_returnsTrue() {
        assertTrue(userSecurityRepository.existsById("ADMIN001"));
    }

    @Test
    void existsById_nonExistent_returnsFalse() {
        assertFalse(userSecurityRepository.existsById("NOUSER"));
    }
}
