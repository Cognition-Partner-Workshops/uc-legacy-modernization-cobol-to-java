package com.carddemo.web.signon;

import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignonService {
    private final UsrsecRepository usrsecRepository;
    private final PasswordEncoder passwordEncoder;

    public SignonService(UsrsecRepository usrsecRepository, PasswordEncoder passwordEncoder) {
        this.usrsecRepository = usrsecRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SignonResponse signon(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            throw new SignonFailureException(HttpStatus.BAD_REQUEST, "Please enter User ID ...");
        }
        if (password == null || password.isBlank()) {
            throw new SignonFailureException(HttpStatus.BAD_REQUEST, "Please enter Password ...");
        }

        String normalizedUserId = userId.toUpperCase(Locale.ROOT);
        String normalizedPassword = password.toUpperCase(Locale.ROOT);
        Usrsec user = usrsecRepository.findById(normalizedUserId)
                .orElseThrow(() -> new SignonFailureException(
                        HttpStatus.UNAUTHORIZED, "User not found. Try again ..."));

        if (!passwordEncoder.matches(normalizedPassword, user.getSecUsrPwd())) {
            throw new SignonFailureException(HttpStatus.UNAUTHORIZED, "Wrong Password. Try again ...");
        }

        boolean admin = "A".equals(user.getSecUsrType());
        String role = admin ? "ROLE_ADMIN" : "ROLE_USER";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getSecUsrId(), null, java.util.List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new SignonResponse(
                user.getSecUsrId(),
                user.getSecUsrFname(),
                user.getSecUsrLname(),
                user.getSecUsrType(),
                role,
                admin ? "COADM01C" : "COMEN01C");
    }
}
