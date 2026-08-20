package com.carddemo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.web.signon.SignonFailureException;
import com.carddemo.web.signon.SignonController;
import com.carddemo.web.signon.SignonRequest;
import com.carddemo.web.signon.SignonOutcome;
import com.carddemo.web.signon.SignonResponse;
import com.carddemo.web.signon.SignonService;
import com.carddemo.web.security.CardDemoUserDetailsService;
import com.carddemo.web.security.LegacyPasswordEncoder;
import com.carddemo.web.security.SecurityConfig;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@WebMvcTest(controllers = SignonController.class)
@ContextConfiguration(classes = {SignonController.class,
        SecurityConfig.class,
        LegacyPasswordEncoder.class})
class SignonControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SignonService signonService;
    @MockBean
    private CardDemoUserDetailsService userDetailsService;

    @Test
    void successfulAdminSignonReturnsTargetProgram() throws Exception {
        when(signonService.signon("ADMIN001", "PASSWORD"))
                .thenReturn(new SignonOutcome(
                        new SignonResponse("ADMIN001", "MARGARET", "GOLD", "A", "ROLE_ADMIN", "COADM01C"),
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(userDetailsService.loadUserByUsername("ADMIN001"))
                .thenReturn(User.withUsername("ADMIN001").password("PASSWORD").authorities("ROLE_ADMIN").build());

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("ADMIN001", "PASSWORD"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"));
    }

    @Test
    void blankIdReturnsBadRequestAndMessage() throws Exception {
        when(signonService.signon(any(), any()))
                .thenThrow(new SignonFailureException(HttpStatus.BAD_REQUEST, "Please enter User ID ..."));

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("", "PASSWORD"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter User ID ..."));
    }

    @Test
    void wrongPasswordReturnsUnauthorizedAndMessage() throws Exception {
        when(signonService.signon(any(), any()))
                .thenThrow(new SignonFailureException(HttpStatus.UNAUTHORIZED, "Wrong Password. Try again ..."));

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("ADMIN001", "NOPE"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }
}
