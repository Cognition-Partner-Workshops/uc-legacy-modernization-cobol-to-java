package com.aws.carddemo.web.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class LegacyPasswordEncoder implements PasswordEncoder {
  @Override
  public String encode(CharSequence rawPassword) {
    return rawPassword == null ? "" : rawPassword.toString();
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return rawPassword != null
        && encodedPassword != null
        && rawPassword.toString().equals(encodedPassword);
  }
}
