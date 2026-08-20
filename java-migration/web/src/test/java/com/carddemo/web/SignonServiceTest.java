package com.carddemo.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import com.carddemo.web.security.LegacyPasswordEncoder;
import com.carddemo.web.signon.SignonFailureException;
import com.carddemo.web.signon.SignonResponse;
import com.carddemo.web.signon.SignonService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SignonServiceTest {
    @Mock
    private UsrsecRepository usrsecRepository;

    private SignonService service;

    @BeforeEach
    void setUp() {
        service = new SignonService(usrsecRepository, new LegacyPasswordEncoder());
    }

    @Test
    void adminSuccess() {
        when(usrsecRepository.findById("ADMIN001"))
                .thenReturn(Optional.of(new Usrsec("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A")));

        SignonResponse response = service.signon("ADMIN001", "PASSWORD").response();

        assertThat(response.role()).isEqualTo("ROLE_ADMIN");
        assertThat(response.nextProgram()).isEqualTo("COADM01C");
        assertThat(response.firstName()).isEqualTo("MARGARET");
    }

    @Test
    void userSuccess() {
        when(usrsecRepository.findById("USER0001"))
                .thenReturn(Optional.of(new Usrsec("USER0001", "LAWRENCE", "THOMAS", "PASSWORD", "U")));

        SignonResponse response = service.signon("USER0001", "PASSWORD").response();

        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.nextProgram()).isEqualTo("COMEN01C");
    }

    @Test
    void wrongPassword() {
        when(usrsecRepository.findById("ADMIN001"))
                .thenReturn(Optional.of(new Usrsec("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A")));

        assertThatThrownBy(() -> service.signon("ADMIN001", "NOPE"))
                .isInstanceOfSatisfying(SignonFailureException.class,
                        e -> assertThat(e.getMessage()).isEqualTo("Wrong Password. Try again ..."));
    }

    @Test
    void unknownUser() {
        when(usrsecRepository.findById("MISSING1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.signon("MISSING1", "PASSWORD"))
                .isInstanceOfSatisfying(SignonFailureException.class,
                        e -> assertThat(e.getMessage()).isEqualTo("User not found. Try again ..."));
    }

    @Test
    void blankId() {
        assertThatThrownBy(() -> service.signon(" ", "PASSWORD"))
                .isInstanceOfSatisfying(SignonFailureException.class,
                        e -> assertThat(e.getMessage()).isEqualTo("Please enter User ID ..."));
    }

    @Test
    void blankPassword() {
        assertThatThrownBy(() -> service.signon("ADMIN001", ""))
                .isInstanceOfSatisfying(SignonFailureException.class,
                        e -> assertThat(e.getMessage()).isEqualTo("Please enter Password ..."));
    }

    @Test
    void lowerCaseInputIsUpperCased() {
        when(usrsecRepository.findById("ADMIN001"))
                .thenReturn(Optional.of(new Usrsec("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A")));

        SignonResponse response = service.signon("admin001", "password").response();

        assertThat(response.userId()).isEqualTo("ADMIN001");
    }
}
