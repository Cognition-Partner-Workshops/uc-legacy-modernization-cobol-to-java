package com.aws.carddemo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.security.TokenService;
import com.aws.carddemo.web.signon.SignonService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignonServiceTest {
  @Mock UsrsecRepository users;
  @Mock PasswordEncoder encoder;
  @Mock TokenService tokens;

  SignonService service;

  @BeforeEach
  void setUp() {
    service = new SignonService(users, encoder, tokens);
  }

  @Test
  void missingUserIdUsesCobolMessage() {
    var result = service.signon(new SignonService.SignonRequest("", "PASSWORD"));
    assertEquals("Please enter User ID ...", result.message());
    assertEquals("userId", result.errorField());
  }

  @Test
  void missingPasswordUsesCobolMessage() {
    var result = service.signon(new SignonService.SignonRequest("USER001", ""));
    assertEquals("Please enter Password ...", result.message());
    assertEquals("password", result.errorField());
  }

  @Test
  void missingUserUsesCobolMessage() {
    when(users.findBySecUsrId("USER001")).thenReturn(Optional.empty());
    var result = service.signon(new SignonService.SignonRequest("user001", "PASSWORD"));
    assertEquals("User not found. Try again ...", result.message());
  }

  @Test
  void wrongPasswordUsesCobolMessage() {
    Usrsec user = user("USER001", "PASSWORD", "U");
    when(users.findBySecUsrId("USER001")).thenReturn(Optional.of(user));
    when(encoder.matches("WRONG", "PASSWORD")).thenReturn(false);
    var result = service.signon(new SignonService.SignonRequest("user001", "wrong"));
    assertEquals("Wrong Password. Try again ...", result.message());
    assertEquals("password", result.errorField());
  }

  @Test
  void lookupFailureUsesCobolMessage() {
    when(users.findBySecUsrId("USER001")).thenThrow(new IllegalStateException("database down"));
    var result = service.signon(new SignonService.SignonRequest("user001", "PASSWORD"));
    assertEquals("Unable to verify the User ...", result.message());
  }

  @Test
  void successfulAdminSignonReturnsRoleAndRoute() {
    Usrsec user = user("ADMIN001", "PASSWORD", "A");
    when(users.findBySecUsrId("ADMIN001")).thenReturn(Optional.of(user));
    when(encoder.matches("PASSWORD", "PASSWORD")).thenReturn(true);
    when(tokens.issue("ADMIN001", "ROLE_ADMIN")).thenReturn("signed-token");
    var result = service.signon(new SignonService.SignonRequest("admin001", "password"));
    assertEquals("signed-token", result.data().token());
    assertEquals("ROLE_ADMIN", result.data().role());
    assertEquals("COADM01C", result.nextRoute());
  }

  private static Usrsec user(String id, String password, String type) {
    Usrsec user = new Usrsec();
    user.setSecUsrId(id);
    user.setSecUsrPwd(password);
    user.setSecUsrType(type);
    return user;
  }
}
