package com.carddemo.web.signon;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;

public record SignonOutcome(
        SignonResponse response,
        List<GrantedAuthority> authorities) {
}
