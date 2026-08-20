package com.carddemo.web.security;

import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CardDemoUserDetailsService implements UserDetailsService {
    private final UsrsecRepository usrsecRepository;

    public CardDemoUserDetailsService(UsrsecRepository usrsecRepository) {
        this.usrsecRepository = usrsecRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usrsec user = usrsecRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        String role = "A".equals(user.getSecUsrType()) ? "ROLE_ADMIN" : "ROLE_USER";
        return User.withUsername(user.getSecUsrId())
                .password(user.getSecUsrPwd())
                .authorities(role)
                .build();
    }
}
