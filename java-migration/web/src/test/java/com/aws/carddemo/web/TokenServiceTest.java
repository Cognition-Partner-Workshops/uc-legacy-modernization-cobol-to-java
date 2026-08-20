package com.aws.carddemo.web;

import static org.junit.jupiter.api.Assertions.*;

import com.aws.carddemo.web.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

class TokenServiceTest {
  @ParameterizedTest
  @ValueSource(strings = {"prod", "production"})
  void productionProfilesRequireConfiguredSecret(String profile) {
    MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles(profile);
    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> new TokenService("", environment));
    assertEquals(
        "CARDDEMO_JWT_SECRET must be configured outside local/test profiles", failure.getMessage());
  }

  @Test
  void noExplicitProfileRequiresConfiguredSecret() {
    MockEnvironment environment = new MockEnvironment();
    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> new TokenService("", environment));
    assertEquals(
        "CARDDEMO_JWT_SECRET must be configured outside local/test profiles", failure.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"local", "test", "dev"})
  void localAndTestProfilesGenerateUsablePerJvmKeys(String profile) {
    MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles(profile);
    TokenService first = new TokenService("", environment);
    TokenService second = new TokenService("", environment);
    String token = first.issue("USER001", "ROLE_USER");

    assertNotNull(first.verify(token));
    assertNull(second.verify(token));
  }
}
