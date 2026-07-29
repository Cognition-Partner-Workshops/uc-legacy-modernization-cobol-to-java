package com.carddemo.usersecurity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.usersecurity.domain.User;
import com.carddemo.usersecurity.service.UserAlreadyExistsException;
import com.carddemo.usersecurity.service.UserNotFoundException;
import com.carddemo.usersecurity.service.UserService;
import com.carddemo.usersecurity.service.WrongPasswordException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void seedsTheTenUsrsecRecords() {
        Page<User> page = userService.list(0, 10);
        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getContent().get(0).getUserId()).isEqualTo("ADMIN001");
        assertThat(page.getContent().get(0).getFirstName()).isEqualTo("MARGARET");
        assertThat(page.getContent().get(0).getLastName()).isEqualTo("GOLD");
    }

    @Test
    void authenticatesAdminAndRegularUsers() {
        assertThat(userService.authenticate("ADMIN001", "PASSWORD").isAdmin()).isTrue();
        assertThat(userService.authenticate("USER0001", "PASSWORD").isAdmin()).isFalse();
        assertThat(userService.authenticate("USER0001", "PASSWORD").getUserType()).isEqualTo("U");
    }

    @Test
    void rejectsWrongPassword() {
        assertThatThrownBy(() -> userService.authenticate("ADMIN001", "BADPWD"))
                .isInstanceOf(WrongPasswordException.class)
                .hasMessage("Wrong Password. Try again ...");
    }

    @Test
    void rejectsUnknownUser() {
        assertThatThrownBy(() -> userService.authenticate("NOSUCHID", "PASSWORD"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User ID NOT found...");
    }

    @Test
    void createsUpdatesAndDeletesUsers() {
        User created = userService.create(new User("TSTUSR1", "TEST", "USER", "pwd12345", "u"));
        assertThat(created.getUserType()).isEqualTo("U");
        assertThat(created.getPassword()).isEqualTo("PWD12345");

        userService.update("TSTUSR1", "CHANGED", "NAME", "NEWPWD1", "A");
        User updated = userService.getById("TSTUSR1");
        assertThat(updated.getFirstName()).isEqualTo("CHANGED");
        assertThat(updated.isAdmin()).isTrue();
        assertThat(userService.authenticate("TSTUSR1", "NEWPWD1")).isNotNull();

        userService.delete("TSTUSR1");
        assertThatThrownBy(() -> userService.getById("TSTUSR1")).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void rejectsDuplicateUserId() {
        assertThatThrownBy(() -> userService.create(new User("ADMIN001", "DUP", "USER", "PASSWORD", "A")))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User ID already exist...");
    }

    @Test
    void updatingUnknownUserFails() {
        assertThatThrownBy(() -> userService.update("NOSUCHID", "A", "B", "PWD", "U"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deletingUnknownUserFails() {
        assertThatThrownBy(() -> userService.delete("NOSUCHID")).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void paginatesTenPerPageLikeCousr00c() {
        Page<User> firstPage = userService.list(0, 4);
        assertThat(firstPage.getContent()).hasSize(4);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.getTotalPages()).isEqualTo(3);

        Page<User> lastPage = userService.list(2, 4);
        assertThat(lastPage.getContent()).hasSize(2);
        assertThat(lastPage.isLast()).isTrue();
    }
}
