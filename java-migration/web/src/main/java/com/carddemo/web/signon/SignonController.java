package com.carddemo.web.signon;

import com.carddemo.web.security.CardDemoUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SignonController {
    private final SignonService signonService;
    private final CardDemoUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public SignonController(
            SignonService signonService,
            CardDemoUserDetailsService userDetailsService,
            SecurityContextRepository securityContextRepository) {
        this.signonService = signonService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/signon")
    public SignonResponse signon(
            @RequestBody SignonRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        SignonOutcome outcome = signonService.signon(request.userId(), request.password());
        UserDetails principal = userDetailsService.loadUserByUsername(outcome.response().userId());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, outcome.authorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return outcome.response();
    }

    @ExceptionHandler(SignonFailureException.class)
    public ResponseEntity<ErrorResponse> handleFailure(SignonFailureException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(exception.getMessage()));
    }

    public record ErrorResponse(String message) {
    }
}
