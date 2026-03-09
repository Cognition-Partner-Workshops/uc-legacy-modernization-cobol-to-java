package com.carddemo.service.online;

import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService implementation replacing COBOL COSGN00C READ-USER-SEC-FILE paragraph.
 *
 * <p>Original COBOL:
 * <pre>
 *   EXEC CICS READ DATASET(WS-USRSEC-FILE) INTO(SEC-USER-DATA)
 *        RIDFLD(WS-USER-ID) RESP(WS-RESP-CD)
 *   ...
 *   EVALUATE WS-RESP-CD
 *     WHEN 0 -> check password, route by type
 *     WHEN 13 -> 'User not found'
 *     WHEN OTHER -> 'Unable to verify'
 * </pre>
 */
@Service
public class CardDemoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CardDemoUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String upperUsername = username.toUpperCase();

        User user = userRepository.findById(upperUsername)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found. Try again ... (ID: " + upperUsername + ")"));

        String role = user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        return new org.springframework.security.core.userdetails.User(
                user.getUsrId(),
                user.getUsrPwd(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
