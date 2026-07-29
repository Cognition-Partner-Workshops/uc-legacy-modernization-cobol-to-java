package com.carddemo.usersecurity.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.usersecurity.domain.User;
import org.junit.jupiter.api.Test;

class UsrsecSeederTest {

    @Test
    void parsesTheFixedWidthEsdsrrdsLayout() {
        User admin = UsrsecSeeder.parse("ADMIN005GRANVILLE           LACHAPELLE          PASSWORDA");
        assertThat(admin.getUserId()).isEqualTo("ADMIN005");
        assertThat(admin.getFirstName()).isEqualTo("GRANVILLE");
        assertThat(admin.getLastName()).isEqualTo("LACHAPELLE");
        assertThat(admin.getPassword()).isEqualTo("PASSWORD");
        assertThat(admin.getUserType()).isEqualTo("A");

        User user = UsrsecSeeder.parse("USER0005LEE                 TING                PASSWORDU");
        assertThat(user.getUserId()).isEqualTo("USER0005");
        assertThat(user.getFirstName()).isEqualTo("LEE");
        assertThat(user.getPassword()).isEqualTo("PASSWORD");
        assertThat(user.getLastName()).isEqualTo("TING");
        assertThat(user.getUserType()).isEqualTo("U");
    }
}
